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
public class Admin_DeviceDetailDTO {

    private String deviceSerialNumber;
    private String name;

    // 💡 [수정] ip/port 제거, edgeSerial 추가
    private String edgeSerial;

    // 💡 [수정] portPath 추가
    private String portPath;

    private List<Admin_DeviceDataRegisterRespDTO> deviceDataList;
}
