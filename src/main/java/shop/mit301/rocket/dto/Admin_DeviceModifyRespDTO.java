package shop.mit301.rocket.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Admin_DeviceModifyRespDTO {
    private String deviceSerialNumber;
    private String name;
    private String ip;
    private int port;
    private boolean updated;

}
