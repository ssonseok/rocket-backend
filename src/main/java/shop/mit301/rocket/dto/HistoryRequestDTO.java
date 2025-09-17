package shop.mit301.rocket.dto;

import lombok.Data;
import java.time.LocalDate;
import java.util.List;

@Data
public class HistoryRequestDTO {
    private LocalDate startDate;
    private LocalDate endDate;
    private String unit;
    private List<YItem> y;

    @Data
    public static class YItem {
        private int unitId;
        private List<Integer> sensorIds; // device_data_id
    }
}