package shop.mit301.rocket.service;

import shop.mit301.rocket.domain.DeviceData;
import shop.mit301.rocket.dto.Admin_DeviceDataMeasureDTO;

import java.util.List;

public interface Admin_DeviceDataMeasureService {
    void saveMeasurement(String serialNumber, List<Double> values);
}
