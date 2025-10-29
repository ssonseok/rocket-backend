package shop.mit301.rocket.service;

import shop.mit301.rocket.domain.DeviceData;
import shop.mit301.rocket.dto.Admin_DeviceDataMeasureDTO;

import java.util.List;

public interface Admin_DeviceDataMeasureService {
    //실시간으로 수신된 데이터값들 저장
    void saveMeasurement(String serialNumber, List<Double> values);
    // 🚨 새로 추가: 장비의 가장 최신 측정 데이터를 JSON 문자열로 반환
    String getLatestDataStreamJson(String serialNumber);
}
