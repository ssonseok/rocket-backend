package shop.mit301.rocket.websocket;

import com.google.gson.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import shop.mit301.rocket.domain.Device;
import shop.mit301.rocket.domain.DeviceData;
import shop.mit301.rocket.domain.Unit;
import shop.mit301.rocket.repository.Admin_DeviceDataRepository;
import shop.mit301.rocket.repository.Admin_DeviceRepository;
import shop.mit301.rocket.repository.Admin_UnitRepository;
import shop.mit301.rocket.service.Admin_DeviceDataMeasureService;
import shop.mit301.rocket.service.EdgeGatewayService;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class EdgeWebSocketHandler extends TextWebSocketHandler {

    // 💡 [의존성 추가] Edge 상태 관리, 측정값 저장, 세션/동기화 관리
    private final EdgeGatewayService edgeGatewayService;
    private final Admin_DeviceDataMeasureService measurementService;
    private final ConnectionRegistry connectionRegistry;

    private final Gson gson = new Gson();

    // --------------------------------------------------------------------------------
    // 1. 장비 등록 Step 1 검증 메서드 (ServiceImpl에서 호출)
    // --------------------------------------------------------------------------------

    /**
     * ServiceImpl에서 호출되어 Edge 장비에 검증 요청을 보내고 동기적으로 응답을 기다립니다.
     * @return Edge로부터 받은 데이터 스트림 개수
     */
    public int verifyDeviceConnection(String edgeSerial, String deviceSerial) throws Exception {
        // 1. Edge 세션 확인
        WebSocketSession session = connectionRegistry.getSession(edgeSerial);
        if (session == null || !session.isOpen()) {
            throw new IllegalStateException("Edge Gateway와의 WebSocket 연결이 활성화되지 않았습니다: " + edgeSerial);
        }

        // 2. 요청 ID 생성 및 메시지 준비 (TEST_REQUEST)
        String commandId = java.util.UUID.randomUUID().toString();

        JsonObject requestJson = new JsonObject();
        requestJson.addProperty("type", "TEST_REQUEST");
        requestJson.addProperty("commandId", commandId);
        requestJson.addProperty("targetSerial", deviceSerial);

        // 3. 응답 대기 시작 (CompletableFuture)
        CompletableFuture<String> future = connectionRegistry.awaitResponse(commandId);

        // 4. 메시지 전송 및 동기적 대기
        session.sendMessage(new TextMessage(requestJson.toString()));
        String responsePayload = future.get(); // 응답이 올 때까지 블로킹

        // 5. 응답 파싱 및 결과 처리
        JsonObject responseJson = JsonParser.parseString(responsePayload).getAsJsonObject();

        if (!"SUCCESS".equalsIgnoreCase(responseJson.get("status").getAsString())) {
            throw new RuntimeException("Edge 장치 연결 테스트 실패: " + responseJson.get("message").getAsString());
        }

        // dataStreamCount 반환
        return responseJson.get("dataStreamCount").getAsInt();
    }


    // --------------------------------------------------------------------------------
    // 2. WebSocket LifeCycle 및 Message Handling
    // --------------------------------------------------------------------------------

    /**
     * Edge 연결 시 호출: 세션 등록 및 DB 상태 업데이트
     */
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String query = session.getUri().getQuery();
        if (query == null || !query.startsWith("edgeSerial=")) {
            session.close(CloseStatus.BAD_DATA);
            return;
        }
        String edgeSerial = query.split("=")[1];

        connectionRegistry.register(edgeSerial, session);
        edgeGatewayService.updateStatus(edgeSerial, "CONNECTED");
        System.out.println("엣지 연결됨: " + edgeSerial);
    }
    // --------------------------------------------------------------------------------
// 3. 장비 상태 보기 기능 (추가)
// --------------------------------------------------------------------------------

    /**
     * ServiceImpl에서 호출되어 Edge 장비에 상태 체크 요청을 보내고 동기적으로 응답을 기다립니다.
     * @param edgeSerial 상태를 확인할 Edge 시리얼 번호
     * @return 응답 페이로드 (JSON String) + 응답속도 정보
     */
    public String checkEdgeStatus(String edgeSerial) throws Exception {
        long startTime = System.currentTimeMillis(); // ⏱️ 응답 속도 측정 시작

        // 1. Edge 세션 확인
        WebSocketSession session = connectionRegistry.getSession(edgeSerial);
        if (session == null || !session.isOpen()) {
            throw new IllegalStateException("Edge Gateway와의 WebSocket 연결이 활성화되지 않았습니다: " + edgeSerial);
        }

        // 2. 요청 ID 생성 및 메시지 준비 (STATUS_CHECK_REQUEST)
        String commandId = java.util.UUID.randomUUID().toString();
        JsonObject requestJson = new JsonObject();
        requestJson.addProperty("type", "STATUS_CHECK_REQUEST");
        requestJson.addProperty("commandId", commandId);

        // 3. 응답 대기 시작
        CompletableFuture<String> future = connectionRegistry.awaitResponse(commandId);

        // 4. 메시지 전송 및 동기적 대기 (5초 타임아웃 설정)
        session.sendMessage(new TextMessage(requestJson.toString()));

        String responsePayload;
        try {
            responsePayload = future.get(5, TimeUnit.SECONDS); // 5초 대기
        } catch (TimeoutException e) {
            connectionRegistry.removeResponse(commandId); // 타임아웃 시 대기 중인 future 제거
            throw new TimeoutException("Edge Gateway 응답 시간 초과 (5초).");
        }

        // 5. 응답 수신 시간 및 응답속도 계산
        long endTime = System.currentTimeMillis();
        long responseTimeMs = endTime - startTime; // 👈 응답 속도

        // 6. 응답 JSON에 응답 속도 정보 및 성공 유무 추가
        JsonObject responseJson = JsonParser.parseString(responsePayload).getAsJsonObject();

        JsonObject finalResult = new JsonObject();
        finalResult.addProperty("responseTimeMs", responseTimeMs);
        finalResult.add("dataPayload", responseJson); // 엣지에서 온 원본 데이터를 dataPayload 필드에 포함

        return finalResult.toString();
    }

    /**
     * Edge로부터 메시지 수신 시 호출: 동기 응답 처리 또는 실시간 데이터 처리
     */
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        JsonObject json = JsonParser.parseString(message.getPayload()).getAsJsonObject();
        String type = json.has("type") ? json.get("type").getAsString() : "UNKNOWN";

        switch (type.toUpperCase()) {
            case "TEST_RESPONSE":
                if (json.has("commandId")) {
                    String commandId = json.get("commandId").getAsString();
                    connectionRegistry.completeResponse(commandId, message.getPayload());
                    System.out.println("테스트 응답 수신: CommandID=" + commandId);
                }
                break;

            case "STATUS_CHECK_RESPONSE":
                if (json.has("commandId")) {
                    String commandId = json.get("commandId").getAsString();
                    connectionRegistry.completeResponse(commandId, message.getPayload());
                    System.out.println("상태 체크 응답 수신: CommandID=" + commandId);
                }
                break;

            case "DATA_STREAM":
                // 💡 수정 완료: 실시간 측정 데이터 처리 및 저장 로직
                if (json.has("serialNumber") && json.has("data")) {
                    String deviceSerial = json.get("serialNumber").getAsString();
                    JsonArray dataArray = json.getAsJsonArray("data");

                    // 1. JsonArray를 List<Double>로 변환
                    List<Double> dataValues = convertJsonArrayToList(dataArray);

                    // 2. measurementService 호출 (서비스 메서드 이름: saveMeasurement)
                    try {
                        measurementService.saveMeasurement(deviceSerial, dataValues);
                    } catch (Exception e) {
                        System.err.println("🚨 측정 데이터 저장 중 오류 발생: " + e.getMessage());
                        // 중요한 데이터이므로 예외가 발생하면 반드시 로그를 남깁니다.
                    }

                } else {
                    System.err.println("🚨 DATA_STREAM에 필수 필드 누락: " + json);
                }
                break;

            default:
                System.err.println("알 수 없는 메시지 타입: " + type);
        }
    }

    /**
     * Edge 연결 종료 시 호출: DB 상태 업데이트 및 세션 정리
     */
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        String edgeSerial = connectionRegistry.unregister(session);
        if (edgeSerial != null) {
            edgeGatewayService.updateStatus(edgeSerial, "DISCONNECTED");
            System.out.println("엣지 연결 종료: " + edgeSerial);
        }
    }
    public boolean isConnected(String edgeSerial) {
        WebSocketSession session = connectionRegistry.getSession(edgeSerial);
        // ConnectionRegistry는 세션이 유효할 때만 반환하므로, null 체크와 isOpen()만 확인하면 됨
        return session != null && session.isOpen();
    }
    private List<Double> convertJsonArrayToList(JsonArray jsonArray) {
        return jsonArray.asList().stream()
                // 각 JsonElement를 Double 타입으로 변환합니다.
                .map(JsonElement::getAsDouble)
                .collect(Collectors.toList());
    }
}
