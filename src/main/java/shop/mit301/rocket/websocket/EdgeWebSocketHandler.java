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

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
public class EdgeWebSocketHandler extends TextWebSocketHandler {

    private final Admin_DeviceRepository deviceRepository;
    private final Admin_DeviceDataRepository deviceDataRepository;
    private final Admin_DeviceDataMeasureService measurementService;
    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();
    private final Gson gson = new Gson();
    private final Admin_UnitRepository unitRepository;
    private final ConnectionRegistry connectionRegistry;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String query = session.getUri().getQuery();

        //  [수정] 쿼리 파라미터 체크를 'edgeSerial='로 변경
        if (query == null || !query.startsWith("edgeSerial=")) {
            session.close(CloseStatus.BAD_DATA);
            return;
        }

        // [수정] 추출하는 키를 'edgeSerial'로 변경
        String edgeSerial = query.split("=")[1];


        //  핵심 수정: 새로운 연결이 들어오면 기존 세션을 명시적으로 닫고 제거합니다.
        WebSocketSession oldSession = sessions.get(edgeSerial);
        if (oldSession != null && oldSession.isOpen()) {
            System.out.println("기존 세션 종료 처리: " + edgeSerial + " (" + oldSession.getId() + ")");
            oldSession.close(CloseStatus.POLICY_VIOLATION); // 정책 위반으로 닫아 엣지 앱이 재연결하도록 유도
            sessions.remove(edgeSerial); // 세션 맵에서 제거
            connectionRegistry.unregister(oldSession); // ConnectionRegistry에서도 제거
        }

        //  [수정] 세션 맵의 키를 'edgeSerial'로 사용
        sessions.put(edgeSerial, session);

        //  [수정] ConnectionRegistry에도 'edgeSerial'로 등록
        connectionRegistry.register(edgeSerial, session);

        System.out.println("엣지 연결됨: " + edgeSerial);
    }

    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        System.out.println("엣지 데이터 수신: " + message.getPayload());
        JsonObject json = JsonParser.parseString(message.getPayload()).getAsJsonObject();

        if (json.has("type") && "TEST_RESPONSE".equalsIgnoreCase(json.get("type").getAsString())) {
            if (json.has("commandId")) {
                String commandId = json.get("commandId").getAsString();
                connectionRegistry.setResponse(commandId, message.getPayload());
                System.out.println("테스트 응답 수신 및 처리 완료: CommandID=" + commandId);
                return;
            }
        }

        if (json.has("status")) {
            if (!"succeed".equalsIgnoreCase(json.get("status").getAsString())) return;
        }
        // status 필드가 없으면(즉, 일반 DATA_STREAM 메시지라면) 계속 진행합니다.

        String serial = json.get("serialNumber").getAsString();

        // 💡 [핵심 수정 시작]: orElseThrow를 제거하고 Optional로 장비 존재 여부만 확인합니다.
        Optional<Device> deviceOptional = deviceRepository.findById(serial);

        if (deviceOptional.isEmpty()) {
            System.err.println("경고: 장비 [" + serial + "]가 아직 DB에 등록되지 않아 데이터 처리를 건너뛰고 세션을 유지합니다.");
            return; // 세션을 닫지 않고 함수 종료
        }

        Device device = deviceOptional.get();
        // 💡 [핵심 수정 종료]

        double[] values = gson.fromJson(json.get("data"), double[].class); // int[] -> double[]로 변경

        List<DeviceData> deviceDataList = deviceDataRepository.findByDevice_DeviceSerialNumber(serial);

        if (deviceDataList.isEmpty()) {

            Unit defaultUnit = unitRepository.findById(1) // 기본 단위
                    .orElseThrow(() -> new RuntimeException("기본 Unit 없음"));

            deviceDataList = new ArrayList<>();
            for (int i = 0; i < values.length; i++) { // values는 이제 double[]
                DeviceData data = DeviceData.builder()
                        .device(device)
                        .name("데이터 " + (i + 1))
                        .dataIndex(i)
                        .isConfigured(false)
                        .min(0)
                        .max(1000)
                        .reference_value(0)
                        .unit(defaultUnit)

                        .build();
                deviceDataRepository.save(data); // DB에 저장 (임시 데이터 확보)
                deviceDataList.add(data);
            }
            System.out.println("DeviceData " + values.length + "개 자동 생성 완료: 시리얼=" + serial);

            return;
        }

        if (!device.is_data_configured()) {
            System.out.println("장비 데이터 설정이 완료되지 않아 측정값 저장을 건너킵니다: " + serial);
            return;
        }

        // 🚨 핵심 수정 3: values가 double[]이므로, 스트림 변환도 그에 맞게 변경합니다.
        List<Double> doubleValues = Arrays.stream(values)
                .boxed() // double[]을 List<Double>로 바로 변환
                .toList();

        try {
            measurementService.saveMeasurement(serial, doubleValues);
            System.out.println("MeasurementData 저장 완료: 시리얼=" + serial);
        } catch (Exception e) {
            System.err.println("측정값 저장 실패: " + e.getMessage());
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        sessions.values().removeIf(s -> s.getId().equals(session.getId()));
        connectionRegistry.unregister(session);
        System.out.println("엣지 연결 종료: " + session.getId());
    }

    public boolean isConnected(String serial) {
        WebSocketSession session = sessions.get(serial);
        return session != null && session.isOpen();
    }
}


