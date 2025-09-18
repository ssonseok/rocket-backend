package shop.mit301.rocket.dto;

import lombok.Data;

import java.util.List;

@Data
public class SensorRequestDTO {
    private String action;
    private List<String> sensorGroups;
}