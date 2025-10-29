package shop.mit301.rocket.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Admin_DeviceVerificationReqDTO {
    private String deviceName; // 장치명 (Step 2에서 사용)
    private String deviceSerial;  // 장비 시리얼 (Device의 PK)
    private String edgeIp;          // Edge IP 주소
    private int edgePort;            // Edge Port 번호
}
