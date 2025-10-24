package shop.mit301.rocket.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EdgeRegisterReqDTO {

    // 엣지 게이트웨이의 고유 식별자 (PK)
    private String edgeSerial;

    // 엣지 장치의 실제 IP 주소
    private String ipAddress;

    // 엣지의 기본 상태 (예: 'DISCONNECTED' 또는 'READY')
    private String status;
}
