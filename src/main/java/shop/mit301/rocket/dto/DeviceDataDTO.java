package shop.mit301.rocket.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeviceDataDTO {
    private int deviceDataId;
    private double min;
    private double max;
    private double referenceValue;
    private String name;

    private int unitId;
    private String unitName;

    private String deviceSerialNumber;
    private String deviceName;
}
