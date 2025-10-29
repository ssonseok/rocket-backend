package shop.mit301.rocket.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EdgeListDTO {

    // 엣지 게이트웨이 식별자
    private String edgeSerial;

    // 현재 IP 주소
    private String ipAddress;

    // 현재 연결 상태
    private String status;

    // 해당 엣지에 연결된 장비의 총 개수 (서비스에서 계산하여 추가)
    private int deviceCount;
}
