package shop.mit301.rocket.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmailAlertRequest {
    private String sensorName;
    private double currentValue;
    private double referenceValue;
}
