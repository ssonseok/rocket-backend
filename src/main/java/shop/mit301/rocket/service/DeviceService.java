package shop.mit301.rocket.service;

import shop.mit301.rocket.domain.UserGraphLayout;
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
    void sendAlert(String toEmail, String sensorName, double currentValue, double referenceValue);
    UserGraphLayout saveOrUpdateLayout(String userId, String dragId, int left, int top, int width, int height);
    List<UserGraphLayout> getLayouts(String userId);
}
