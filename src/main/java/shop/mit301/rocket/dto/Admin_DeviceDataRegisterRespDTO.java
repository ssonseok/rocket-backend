package shop.mit301.rocket.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Admin_DeviceDataRegisterRespDTO {
    private String name;
    private double min;
    private double max;
    private double referenceValue;
    private int unitId;
    private boolean saved;         // DB 저장 여부
}
