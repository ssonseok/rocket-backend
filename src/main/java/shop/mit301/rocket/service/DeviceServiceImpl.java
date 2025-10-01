package shop.mit301.rocket.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import shop.mit301.rocket.domain.DeviceData;
import shop.mit301.rocket.domain.MeasurementData;
import shop.mit301.rocket.domain.MeasurementDataId;
import shop.mit301.rocket.domain.PredictionData;
import shop.mit301.rocket.dto.HistoryRequestDTO;
import shop.mit301.rocket.dto.HistoryResponseDTO;
import shop.mit301.rocket.dto.SensorResponseDTO;
import shop.mit301.rocket.repository.DeviceDataRepository;
import shop.mit301.rocket.repository.MeasurementDataRepository;
import shop.mit301.rocket.repository.PredictionDataRepository;
import shop.mit301.rocket.repository.UnitRepository;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Log4j2
public class DeviceServiceImpl implements DeviceService {

    private final PredictionDataRepository predictionDataRepository;
    private final MeasurementDataRepository measurementDataRepository;
    private final UnitRepository unitRepository;
    private final DeviceDataRepository deviceDataRepository;

    @Override
    public HistoryResponseDTO getHistory(HistoryRequestDTO request) {
        List<Integer> sensorIds = Optional.ofNullable(request.getY())
                .orElse(Collections.emptyList())
                .stream()
                .flatMap(y -> Optional.ofNullable(y.getSensorIds()).orElse(Collections.emptyList()).stream())
                .distinct()
                .collect(Collectors.toList());

        List<Integer> unitIds = request.getY().stream()
                .map(HistoryRequestDTO.YItem::getUnitId)
                .distinct()
                .collect(Collectors.toList());

        List<MeasurementData> measurements = measurementDataRepository.findAllByFilter(
                request.getStartDate().atStartOfDay(),
                request.getEndDate().atTime(23, 59, 59),
                unitIds,
                sensorIds
        );

        List<HistoryResponseDTO.UnitInfo> unitInfos = unitRepository.findAllById(unitIds).stream()
                .map(u -> new HistoryResponseDTO.UnitInfo(u.getUnitid(), u.getUnit()))
                .collect(Collectors.toList());

        Map<LocalDateTime, List<MeasurementData>> grouped = measurements.stream()
                .collect(Collectors.groupingBy(m -> truncateByUnit(m.getId().getMeasurementdate(), request.getUnit())));

        List<HistoryResponseDTO.TimestampGroup> data = grouped.entrySet().stream()
                .map(entry -> {
                    List<HistoryResponseDTO.SensorValue> values = entry.getValue().stream()
                            .map(m -> new HistoryResponseDTO.SensorValue(
                                    m.getDevicedata().getDevicedataid(),
                                    m.getDevicedata().getUnit().getUnitid(),
                                    m.getMeasurementvalue(),
                                    m.getDevicedata().getReference_value() // 기준값 포함
                            )).collect(Collectors.toList());

                    return new HistoryResponseDTO.TimestampGroup(entry.getKey(), values);
                })
                .sorted(Comparator.comparing(HistoryResponseDTO.TimestampGroup::getTimestamp))
                .collect(Collectors.toList());

        return new HistoryResponseDTO(unitInfos, data);
    }

    @Override
    public LocalDateTime truncateByUnit(LocalDateTime dateTime, String unit) {
        switch (unit.toUpperCase()) {
            case "DAILY":
                return dateTime.toLocalDate().atStartOfDay();
            case "WEEKLY":
                return dateTime.toLocalDate().with(DayOfWeek.MONDAY).atStartOfDay();
            case "MONTHLY":
                return dateTime.toLocalDate().withDayOfMonth(1).atStartOfDay();
            case "YEARLY":
                return dateTime.toLocalDate().withDayOfYear(1).atStartOfDay();
            default:
                throw new IllegalArgumentException("Unknown unit: " + unit);
        }
    }

    @Override
    public HistoryResponseDTO getPrediction(HistoryRequestDTO request) {
        if (request.getStartDate() == null || request.getEndDate() == null) {
            throw new IllegalArgumentException("startDate와 endDate는 필수 값입니다.");
        }

        List<Integer> sensorIds = request.getY().stream()
                .filter(y -> y.getSensorIds() != null)
                .flatMap(y -> y.getSensorIds().stream())
                .distinct()
                .collect(Collectors.toList());

        List<Integer> unitIds = request.getY().stream()
                .map(HistoryRequestDTO.YItem::getUnitId)
                .distinct()
                .collect(Collectors.toList());

        List<PredictionData> predictions = predictionDataRepository.findAllByFilter(
                request.getStartDate().atStartOfDay(),
                request.getEndDate().atTime(23, 59, 59),
                unitIds,
                sensorIds
        );

        List<HistoryResponseDTO.UnitInfo> unitInfos = unitRepository.findAllById(unitIds).stream()
                .map(u -> new HistoryResponseDTO.UnitInfo(u.getUnitid(), u.getUnit()))
                .collect(Collectors.toList());

        Map<LocalDateTime, List<PredictionData>> grouped = predictions.stream()
                .collect(Collectors.groupingBy(p -> truncateByUnit(p.getId().getPredictiondate(), request.getUnit())));

        List<HistoryResponseDTO.TimestampGroup> data = grouped.entrySet().stream()
                .map(entry -> {
                    List<HistoryResponseDTO.SensorValue> values = entry.getValue().stream()
                            .map(p -> new HistoryResponseDTO.SensorValue(
                                    p.getDevice_data().getDevicedataid(),
                                    p.getDevice_data().getUnit().getUnitid(),
                                    p.getPredicted_value(),
                                    p.getDevice_data().getReference_value()  // 기준값 포함
                            )).collect(Collectors.toList());

                    return new HistoryResponseDTO.TimestampGroup(entry.getKey(), values);
                })
                .sorted(Comparator.comparing(HistoryResponseDTO.TimestampGroup::getTimestamp))
                .collect(Collectors.toList());

        return new HistoryResponseDTO(unitInfos, data);
    }

    @Override
    public List<SensorResponseDTO> collectAndSend(List<Integer> sensorIds) {
        List<DeviceData> sensors = deviceDataRepository.findByDevicedataidIn(sensorIds);
        List<MeasurementData> measurementsToSave = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();

        for (DeviceData sensor : sensors) {
            Double value;
            try {
                value = getSensorValue(sensor.getDevice().getDeviceSerialNumber(), sensor.getDevicedataid());
            } catch (Exception e) {
                log.error("센서 값 조회 실패 - sensorId: {}", sensor.getDevicedataid(), e);
                continue;
            }

            if (value == null) {
                log.warn("센서 값이 null입니다 - sensorId: {}", sensor.getDevicedataid());
                continue;
            }

            MeasurementData measurement = MeasurementData.builder()
                    .id(new MeasurementDataId(now, sensor.getDevicedataid()))
                    .measurementvalue(value)
                    .devicedata(sensor)
                    .build();

            measurementsToSave.add(measurement);
        }

        measurementDataRepository.saveAll(measurementsToSave);

        List<SensorResponseDTO> responses = new ArrayList<>();
        for (DeviceData sensor : sensors) {
            MeasurementData latestMeasurement = measurementDataRepository
                    .findTopByDevicedataOrderByIdMeasurementdateDesc(sensor)
                    .orElse(null);

            if (latestMeasurement == null) continue;

            responses.add(SensorResponseDTO.builder()
                    .deviceSerial(sensor.getDevice().getDeviceSerialNumber())
                    .sensorId(sensor.getDevicedataid())
                    .name(sensor.getName())
                    .value(latestMeasurement.getMeasurementvalue())
                    .unitId(sensor.getUnit().getUnitid())
                    .referenceValue(sensor.getReference_value())
                    .timestamp(latestMeasurement.getId().getMeasurementdate().toString())
                    .build());
        }

        return responses;
    }

    @Override
    public Double getSensorValue(String deviceSerial, Integer sensorId) {
        // 예: TCP 소켓, MQTT 메시지, HTTP 요청 등 센서와의 통신 구현

        // 아래는 예시 — 실제 구현 시 센서 프로토콜에 따라 변경
        // 예: 센서 데이터가 HTTP로 온다면:
        // ResponseEntity<Double> response = restTemplate.getForEntity(url, Double.class);
        // return response.getBody();

        // TODO: 실제 센서 통신 로직 구현 필요
        throw new UnsupportedOperationException("센서 통신 구현 필요");
    }
    @Override
    public List<SensorResponseDTO> getAllSensors() {
        List<DeviceData> sensors = deviceDataRepository.findAll();
        // 값은 테스트용 기본값으로 셋팅
        return sensors.stream()
                .map(sensor -> SensorResponseDTO.builder()
                        .deviceSerial(sensor.getDevice().getDeviceSerialNumber())
                        .sensorId(sensor.getDevicedataid())
                        .name(sensor.getName())
                        .value(0.0) // 테스트용 기본값
                        .unitId(sensor.getUnit().getUnitid())
                        .referenceValue(sensor.getReference_value())
                        .timestamp(LocalDateTime.now().toString())
                        .build())
                .collect(Collectors.toList());
    }
}
