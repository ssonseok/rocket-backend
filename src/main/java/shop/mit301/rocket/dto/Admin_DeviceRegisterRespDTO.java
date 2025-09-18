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
    private String ip;
    private int port;
    private boolean testSuccess;          // 테스트 통신 성공 여부
    private List<DeviceDataDTO> sensors;  // 연결된 센서 정보 (자동 생성)
}
