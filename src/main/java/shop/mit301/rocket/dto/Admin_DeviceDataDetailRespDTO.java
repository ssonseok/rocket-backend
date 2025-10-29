package shop.mit301.rocket.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Admin_DeviceDataDetailRespDTO {
    private long deviceDataId;
    private String name;
    private String unitName;
    private double min;
    private double max;
    private double referenceValue;
}
