package shop.mit301.rocket.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Admin_DeviceRegisterReqDTO {

    private String deviceSerialNumber;
    private String edgeSerial;

    private String portPath;

    private String name;

    // 이외 필드 (예: description 등)
}
