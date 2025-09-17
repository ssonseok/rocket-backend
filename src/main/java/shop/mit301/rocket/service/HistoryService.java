package shop.mit301.rocket.service;

import shop.mit301.rocket.dto.HistoryRequestDTO;
import shop.mit301.rocket.dto.HistoryResponseDTO;

import java.time.LocalDateTime;

public interface HistoryService {
    HistoryResponseDTO getHistory(HistoryRequestDTO request);
    LocalDateTime truncateByUnit(LocalDateTime dateTime, String unit);
}
