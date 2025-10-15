package shop.mit301.rocket.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Admin_DeviceDataMeasureDTO {
    private String deviceSerialNumber;
    private List<Integer> values;  // 엣지에서 보내는 센서값 6개
    private LocalDateTime timestamp;
}
