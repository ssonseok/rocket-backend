package shop.mit301.rocket.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import shop.mit301.rocket.domain.MeasurementData;
import shop.mit301.rocket.domain.PredictionData;
import shop.mit301.rocket.dto.HistoryRequestDTO;
import shop.mit301.rocket.dto.HistoryResponseDTO;
import shop.mit301.rocket.repository.MeasurementDataRepository;
import shop.mit301.rocket.repository.PredictionDataRepository;
import shop.mit301.rocket.repository.UnitRepository;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DeviceServiceImpl implements DeviceService {

    private final PredictionDataRepository predictionDataRepository;
    private final MeasurementDataRepository measurementDataRepository;
    private final UnitRepository unitRepository;

    @Override
    public HistoryResponseDTO getHistory(HistoryRequestDTO request) {
        List<Integer> sensorIds = request.getY().stream()
                .flatMap(y -> y.getSensorIds().stream())
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
                .map(u -> new HistoryResponseDTO.UnitInfo(u.getUnit_id(), u.getUnit()))
                .collect(Collectors.toList());

        Map<LocalDateTime, List<MeasurementData>> grouped = measurements.stream()
                .collect(Collectors.groupingBy(m -> truncateByUnit(m.getId().getMeasurement_date(), request.getUnit())));

        List<HistoryResponseDTO.TimestampGroup> data = grouped.entrySet().stream()
                .map(entry -> {
                    List<HistoryResponseDTO.SensorValue> values = entry.getValue().stream()
                            .map(m -> new HistoryResponseDTO.SensorValue(
                                    m.getDevice_data().getDevice_data_id(),
                                    m.getDevice_data().getUnit().getUnit_id(),
                                    m.getMeasurement_value()
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
        List<Integer> sensorIds = request.getY().stream()
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
                .map(u -> new HistoryResponseDTO.UnitInfo(u.getUnit_id(), u.getUnit()))
                .collect(Collectors.toList());

        Map<LocalDateTime, List<PredictionData>> grouped = predictions.stream()
                .collect(Collectors.groupingBy(p -> truncateByUnit(p.getId().getPrediction_date(), request.getUnit())));

        List<HistoryResponseDTO.TimestampGroup> data = grouped.entrySet().stream()
                .map(entry -> {
                    List<HistoryResponseDTO.SensorValue> values = entry.getValue().stream()
                            .map(p -> new HistoryResponseDTO.SensorValue(
                                    p.getDevice_data().getDevice_data_id(),
                                    p.getDevice_data().getUnit().getUnit_id(),
                                    p.getPredicted_value()
                            )).collect(Collectors.toList());

                    return new HistoryResponseDTO.TimestampGroup(entry.getKey(), values);
                })
                .sorted(Comparator.comparing(HistoryResponseDTO.TimestampGroup::getTimestamp))
                .collect(Collectors.toList());

        return new HistoryResponseDTO(unitInfos, data);
    }
}
