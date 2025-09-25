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
    private String status;
    private String dataStatus;
    private String responseData;
    private long responseTimeMs;
}
