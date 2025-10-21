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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
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
        if (query == null || !query.startsWith("deviceSerial=")) {
            session.close(CloseStatus.BAD_DATA);
            return;
        }
        String serial = query.split("=")[1];
        sessions.put(serial, session);

        connectionRegistry.register(serial, session);

        System.out.println("엣지 연결됨: " + serial);
    }

    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        System.out.println("엣지 데이터 수신: " + message.getPayload());
        JsonObject json = JsonParser.parseString(message.getPayload()).getAsJsonObject();

        // ----------------------------------------------------------------------
        // ✅ 1. 테스트 응답 메시지 처리 로직 추가
        // ----------------------------------------------------------------------
        if (json.has("type") && "TEST_RESPONSE".equalsIgnoreCase(json.get("type").getAsString())) {
            if (json.has("commandId")) {
                String commandId = json.get("commandId").getAsString();
                // ConnectionRegistry에 응답을 전달하여 Service 스레드를 해제합니다.
                connectionRegistry.setResponse(commandId, message.getPayload());
                System.out.println("테스트 응답 수신 및 처리 완료: CommandID=" + commandId);
                return; // 테스트 응답 처리를 완료하고 기존 측정 데이터 로직은 건너뜁니다.
            }
        }
        // ----------------------------------------------------------------------

        if (!"succeed".equalsIgnoreCase(json.get("status").getAsString())) return;

        String serial = json.get("serialNumber").getAsString();
        int[] values = gson.fromJson(json.get("data"), int[].class);

        Device device = deviceRepository.findById(serial)
                .orElseThrow(() -> new RuntimeException("등록되지 않은 장비: " + serial));

        List<DeviceData> deviceDataList = deviceDataRepository.findByDevice_DeviceSerialNumber(serial);

        if (deviceDataList.isEmpty()) {
            Unit defaultUnit = unitRepository.findById(1) // 기본 단위
                    .orElseThrow(() -> new RuntimeException("기본 Unit 없음"));

            deviceDataList = new ArrayList<>();
            for (int i = 0; i < values.length; i++) {
                DeviceData data = DeviceData.builder()
                        .device(device)
                        .name("데이터 " + (i + 1))
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
            System.out.println("장비 데이터 설정이 완료되지 않아 측정값 저장을 건너뜁니다: " + serial);
            return;
        }

        List<Double> doubleValues = Arrays.stream(values)
                .mapToDouble(i -> (double) i)
                .boxed()
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


