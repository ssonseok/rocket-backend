package shop.mit301.rocket.service;

import shop.mit301.rocket.dto.HistoryRequestDTO;
import shop.mit301.rocket.dto.HistoryResponseDTO;
import shop.mit301.rocket.dto.SensorResponseDTO;

import java.time.LocalDateTime;
import java.util.List;

public interface DeviceService {
    HistoryResponseDTO getHistory(HistoryRequestDTO request);
    LocalDateTime truncateByUnit(LocalDateTime dateTime, String unit);
    HistoryResponseDTO getPrediction(HistoryRequestDTO request);
    List<SensorResponseDTO> collectAndSend(List<Integer> sensorIds);
    Double getSensorValue(String deviceSerial, Integer sensorId);
    List<SensorResponseDTO> getAllSensors();
}
