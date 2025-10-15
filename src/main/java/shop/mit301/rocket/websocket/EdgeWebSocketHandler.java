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

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String query = session.getUri().getQuery();
        if (query == null || !query.startsWith("deviceSerial=")) {
            session.close(CloseStatus.BAD_DATA);
            return;
        }
        String serial = query.split("=")[1];
        sessions.put(serial, session);
        System.out.println("엣지 연결됨: " + serial);
    }

    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        System.out.println("엣지 데이터 수신: " + message.getPayload());
        JsonObject json = JsonParser.parseString(message.getPayload()).getAsJsonObject();

        if (!"succeed".equalsIgnoreCase(json.get("status").getAsString())) return;

        String serial = json.get("serialNumber").getAsString();
        int[] values = gson.fromJson(json.get("data"), int[].class);

        // 1. Device 조회
        Device device = deviceRepository.findById(serial)
                .orElseThrow(() -> new RuntimeException("등록되지 않은 장비: " + serial));

        // 2. DeviceData 조회
        List<DeviceData> deviceDataList = deviceDataRepository.findByDevice_DeviceSerialNumber(serial);

        // 3. DeviceData 없으면 엣지 데이터 길이만큼 생성
        if (deviceDataList.isEmpty()) {
            Unit defaultUnit = unitRepository.findById(1) // 기본 단위
                    .orElseThrow(() -> new RuntimeException("기본 Unit 없음"));

            deviceDataList = new ArrayList<>();
            for (int i = 0; i < values.length; i++) {
                // ... (기존 DeviceData Builder 로직 유지) ...
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

            // ✨ 수정: 임시 데이터가 생성된 경우, 아직 최종 등록 전이므로 측정값 저장을 건너뛰고 함수 종료
            return;
        }

        // 4. MeasurementData 저장 (✨ 수정된 부분: 장비 설정 완료 플래그 확인)

        // 장치 데이터 설정이 완료되지 않았다면 측정을 건너뜁니다.
        // Device 엔티티에 추가된 isDataConfigured() 메서드를 사용합니다.

        if (!device.is_data_configured()) {
            System.out.println("장비 데이터 설정이 완료되지 않아 측정값 저장을 건너뜁니다: " + serial);
            return;
        }

        // ✨ 최종 등록이 완료된 경우에만 아래 로직 실행
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
        System.out.println("엣지 연결 종료: " + session.getId());
    }

    public boolean isConnected(String serial) {
        WebSocketSession session = sessions.get(serial);
        return session != null && session.isOpen();
    }
}


