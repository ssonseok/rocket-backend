package shop.mit301.rocket.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Admin_DeviceStatusRespDTO {
    private String deviceSerialNumber;
    private String deviceName;
    private String edgeSerial;

    // Edge Gateway의 DB 상태 (CONNECTED / DISCONNECTED / ERROR)
    private String dbStatus;

    // WebSocket 핸들러를 통한 실시간 연결 상태
    private boolean wsConnected;

    // 장비로부터 마지막으로 데이터를 수신한 시간
    private LocalDateTime lastDataReceived;

    // 평균 응답 속도 (ms)
    private Long responseTimeMs;
}
