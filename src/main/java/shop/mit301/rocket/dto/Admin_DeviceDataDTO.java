package shop.mit301.rocket.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Admin_DeviceDataDTO {
    private String name;  // UI 표시용 이름
    private String unit;  // 단위 문자열 (백엔드에서 int PK로 변환)
}
