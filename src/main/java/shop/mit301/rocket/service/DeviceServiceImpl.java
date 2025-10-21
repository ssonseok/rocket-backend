package shop.mit301.rocket.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
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
import java.time.format.DateTimeFormatter;
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
    private final RestTemplate restTemplate;

    private LocalDateTime convertToLocalDateTime(Object periodObj, String unit) {
        if (periodObj instanceof java.sql.Date) {
            return ((java.sql.Date) periodObj).toLocalDate().atStartOfDay();
        } else if (periodObj instanceof java.sql.Timestamp) {
            return ((java.sql.Timestamp) periodObj).toLocalDateTime();
        } else if (periodObj instanceof String) {
            // 유연하게 대응
            String str = (String) periodObj;
            if (str.length() == 10) {
                return LocalDateTime.parse(str + " 00:00:00", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            } else {
                return LocalDateTime.parse(str, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            }
        } else {
            return LocalDateTime.now(); // fallback
        }
    }

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

        LocalDateTime start = request.getStartDate().atStartOfDay();
        LocalDateTime end = request.getEndDate().atTime(23, 59, 59);
        String unit = request.getUnit().toLowerCase();

        List<Object[]> rawData;

        switch (unit) {
            case "daily": unit = "day"; break;
            case "weekly": unit = "week"; break;
            case "monthly": unit = "month"; break;
            case "yearly": unit = "year"; break;
        }

        // 🔁 쿼리 분기
        switch (unit) {
            case "day":
                rawData = measurementDataRepository.findAggregatedDaily(start, end, unitIds, sensorIds);
                break;
            case "week":
                rawData = measurementDataRepository.findAggregatedWeekly(start, end, unitIds, sensorIds);
                break;
            case "month":
                rawData = measurementDataRepository.findAggregatedMonthly(start, end, unitIds, sensorIds);
                break;
            case "year":
                rawData = measurementDataRepository.findAggregatedYearly(start, end, unitIds, sensorIds);
                break;
            default:
                throw new IllegalArgumentException("Invalid unit: " + unit);
        }

        // 📦 단위 정보 조회
        List<HistoryResponseDTO.UnitInfo> unitInfos = unitRepository.findAllById(unitIds).stream()
                .map(u -> new HistoryResponseDTO.UnitInfo(u.getUnitid(), u.getUnit()))
                .collect(Collectors.toList());

        // 🧩 period(LocalDateTime) -> List<SensorValue> 매핑
        Map<LocalDateTime, List<HistoryResponseDTO.SensorValue>> grouped = new LinkedHashMap<>();

        for (Object[] row : rawData) {
            LocalDateTime period = convertToLocalDateTime(row[0], unit);
            Integer unitId = ((Number) row[1]).intValue();
            Integer sensorId = ((Number) row[2]).intValue();
            Double avgValue = ((Number) row[3]).doubleValue();

            Double referenceValue = 0.0;
            if (row.length > 4 && row[4] != null) {
                referenceValue = ((Number) row[4]).doubleValue();
            }

            grouped.computeIfAbsent(period, k -> new ArrayList<>())
                    .add(new HistoryResponseDTO.SensorValue(sensorId, unitId, avgValue, referenceValue));
        }

        List<HistoryResponseDTO.TimestampGroup> data = grouped.entrySet().stream()
                .map(entry -> new HistoryResponseDTO.TimestampGroup(entry.getKey(), entry.getValue()))
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
        String url = String.format("http://localhost:8080/api/sensors/%s/%d/value", deviceSerial, sensorId);

        try {
            ResponseEntity<Double> response = restTemplate.getForEntity(url, Double.class);
            Double value = response.getBody();

            if (value != null) {
                return value;
            } else {
                throw new RuntimeException("센서 데이터가 없습니다.");
            }
        } catch (Exception e) {
            throw new RuntimeException("센서 통신 실패", e);
        }
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
