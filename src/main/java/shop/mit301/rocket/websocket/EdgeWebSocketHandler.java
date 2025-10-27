package shop.mit301.rocket.websocket;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
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

            case "DATA_STREAM":
                if (json.has("serialNumber") && json.has("data")) {
                    String serial = json.get("serialNumber").getAsString();
                    double[] valuesArray = gson.fromJson(json.get("data"), double[].class);
                    List<Double> doubleValues = Arrays.stream(valuesArray).boxed().toList();

                    try {
                        // ✅ 핵심: Device 등록 + DeviceData 존재 확인 후 바로 저장
                        measurementService.saveMeasurement(serial, doubleValues);
                        System.out.println("MeasurementData 저장 완료: 시리얼=" + serial);
                    } catch (Exception e) {
                        System.err.println("측정값 저장 실패: " + e.getMessage());
                    }
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
}
