package shop.mit301.rocket.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SensorResponseDTO {
    private String deviceSerial;
    private int sensorId;
    private String name;
    private double value;
    private int unitId;
    private double referenceValue;
    private String timestamp;
}