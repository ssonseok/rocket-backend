package shop.mit301.rocket.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Admin_DeviceDataRegisterReqDTO {
    private String name;           // 이름 (사용자가 수정 가능)
    private double min;            // 최소값
    private double max;            // 최대값
    private double referenceValue; // 기준값
    private int unitId;            // DB 단위 PK
}
