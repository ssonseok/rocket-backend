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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class Admin_DeviceDataMeasureServiceImpl implements Admin_DeviceDataMeasureService {

    // 필요한 Repository는 DeviceData와 MeasurementData입니다.
    private final Admin_DeviceRepository deviceRepository;
    private final Admin_DeviceDataRepository deviceDataRepository;
    private final Admin_MeasurementDataRepository measurementDataRepository;

    /**
     * Edge로부터 수신한 실시간 측정값 목록을 DB에 저장합니다.
     */
    @Override
    @Transactional
    public void saveMeasurement(String deviceSerial, List<Double> values) {

        // 1. 해당 장비의 모든 DeviceData 목록을 dataIndex를 기준으로 Map에 저장합니다.
        List<DeviceData> deviceDataList = deviceDataRepository.findByDevice_DeviceSerialNumber(deviceSerial);

        if (deviceDataList.isEmpty()) {
            System.err.println("경고: 장비 [" + deviceSerial + "]에 등록된 데이터 스트림이 없습니다. 측정값 저장 스킵.");
            return;
        }

        // dataIndex를 키로 사용하여 빠르게 DeviceData 엔티티를 찾을 수 있도록 Map으로 변환
        Map<Integer, DeviceData> dataIndexMap = deviceDataList.stream()
                .collect(Collectors.toMap(DeviceData::getDataIndex, data -> data));

        // 2. 수신된 측정값(values)을 반복하면서 MeasurementData를 생성 및 수집합니다.
        // 💡 PK 충돌 방지 및 일관성 유지를 위해 시간은 루프 밖에서 한 번만 생성합니다.
        LocalDateTime now = LocalDateTime.now();
        List<MeasurementData> measurements = new ArrayList<>(values.size());

        for (int i = 0; i < values.size(); i++) {

            // 수신된 값의 인덱스(i)를 DeviceData의 dataIndex와 매핑합니다.
            DeviceData deviceData = dataIndexMap.get(i);
            Double value = values.get(i);

            if (deviceData == null) {
                System.err.println("경고: 장비 [" + deviceSerial + "]의 Data Index [" + i + "]에 해당하는 DeviceData를 찾을 수 없습니다. 값: " + value);
                continue; // 매핑되지 않은 값은 스킵
            }

            // 3. MeasurementData 엔티티 생성
            // 🚨 [수정됨] 복합 키 구조에 맞춰 MeasurementDataId 객체를 생성하여 .id()에 전달
            MeasurementData measurement = MeasurementData.builder()
                    .id(new MeasurementDataId(now, deviceData.getDevicedataid())) // 복합키 (시간 + DeviceData PK)
                    .measurementvalue(value) // measurementvalue 필드 사용
                    .devicedata(deviceData)
                    .build();

            measurements.add(measurement);
        }

        // 4. 💡 [수정됨] 성능 최적화를 위해 saveAll()을 사용하여 배치 삽입을 유도합니다.
        if (!measurements.isEmpty()) {
            measurementDataRepository.saveAll(measurements);
        }

        System.out.println("장비 [" + deviceSerial + "]의 측정값 " + measurements.size() + "개가 DB에 배치 저장 완료되었습니다.");
    }
}