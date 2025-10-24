package shop.mit301.rocket.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EdgeModifyReqDTO {

    // 수정할 엣지 게이트웨이 식별자 (필수)
    private String edgeSerial;

    // 새로운 IP 주소 (선택적 수정)
    private String ipAddress;

    // 연결 상태 수정 (예: 관리자가 수동으로 'ERROR'로 변경)
    private String status;
}
