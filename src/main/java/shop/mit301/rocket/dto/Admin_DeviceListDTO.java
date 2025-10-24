package shop.mit301.rocket.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Admin_DeviceListDTO {

    private String deviceSerialNumber;
    private String deviceName;
    private LocalDateTime createdDate;

    // 💡 [추가] 엣지 시리얼 및 포트 경로
    private String edgeSerial;
    private String portPath;

    private List<String> dataNames;
}