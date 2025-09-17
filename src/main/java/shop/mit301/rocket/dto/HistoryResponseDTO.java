package shop.mit301.rocket.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class HistoryResponseDTO {
    private List<UnitInfo> y;
    private List<TimestampGroup> data;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class UnitInfo {
        private int unitId;
        private String name;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class TimestampGroup {
        private LocalDateTime timestamp;
        private List<SensorValue> values;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class SensorValue {
        private int sensorId;
        private int unitId;
        private double value;
    }
}
