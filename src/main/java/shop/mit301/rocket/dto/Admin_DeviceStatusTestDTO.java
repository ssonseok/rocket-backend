package shop.mit301.rocket.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Admin_DeviceStatusTestDTO {

    private String deviceSerialNumber;
    private String name;
    private String status; // 성공/실패
    private String dataStatus; // OK/ERROR_DATA

    // 💡 [추가] 엣지 시리얼 및 포트 경로
    private String edgeSerial;
    private String portPath;

    private String responseData;
    private long responseTimeMs;
}
