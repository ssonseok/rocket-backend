package shop.mit301.rocket.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import shop.mit301.rocket.domain.Device;
import shop.mit301.rocket.domain.DeviceData;
import shop.mit301.rocket.domain.MeasurementData;
import shop.mit301.rocket.domain.MeasurementDataId;
import shop.mit301.rocket.dto.Admin_DeviceDataMeasureDTO;
import shop.mit301.rocket.repository.Admin_DeviceDataRepository;
import shop.mit301.rocket.repository.Admin_DeviceRepository;
import shop.mit301.rocket.repository.Admin_MeasurementDataRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class Admin_DeviceDataMeasureServiceImpl implements Admin_DeviceDataMeasureService{

    private final Admin_DeviceRepository deviceRepository;
    private final Admin_DeviceDataRepository deviceDataRepository;
    private final Admin_MeasurementDataRepository measurementRepository;

    @Override
    @Transactional
    public void saveMeasurement(String serialNumber, List<Double> values) {
        List<DeviceData> dataList = deviceDataRepository.findByDevice_DeviceSerialNumber(serialNumber);

        if (dataList.size() != values.size()) {
            throw new RuntimeException("센서 개수 불일치 (DB=" + dataList.size() + ", 받은값=" + values.size() + ")");
        }

        LocalDateTime now = LocalDateTime.now();
        for (int i = 0; i < dataList.size(); i++) {
            DeviceData data = dataList.get(i);
            MeasurementData measurement = MeasurementData.builder()
                    .id(new MeasurementDataId(now, data.getDevicedataid()))
                    .measurementvalue(values.get(i))
                    .devicedata(data)
                    .build();
            measurementRepository.save(measurement);
        }
    }
}
