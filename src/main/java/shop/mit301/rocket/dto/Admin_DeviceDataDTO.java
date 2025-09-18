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
    private String name;           // 데이터 명칭
    private double min;
    private double max;
    private double referenceValue;
    private int unitId;            // 단위 ID (DB FK)
}
