package shop.mit301.rocket.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Admin_MeasurementReqDTO {
    // 장비 시리얼 번호
    private String deviceSerialNumber;

    // 엣지에서 전송된 센서 측정값 리스트 (Double 타입이 실수형 데이터 처리에 적합)
    private List<Double> values;
}
