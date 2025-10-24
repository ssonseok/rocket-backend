package shop.mit301.rocket.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Admin_DeviceRegisterRespDTO {

    private String deviceSerialNumber;
    private String name;

    // 💡 [제거] ip/port 제거
    // private String ip;
    // private int port;

    private boolean testSuccess;
    private int dataCount;
    private List<DeviceDataDTO> sensors; // 불필요 시 제거
}
