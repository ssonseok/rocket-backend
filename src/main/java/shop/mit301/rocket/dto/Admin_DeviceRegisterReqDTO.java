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

    // 💡 [수정] ip/port 제거, edgeSerial 추가
    private String edgeSerial;

    // 💡 [수정] portPath 추가
    private String portPath;

    private String name;

    // 이외 필드 (예: description 등)
}
