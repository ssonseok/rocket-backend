package shop.mit301.rocket.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import shop.mit301.rocket.dto.HistoryRequestDTO;
import shop.mit301.rocket.dto.HistoryResponseDTO;
import shop.mit301.rocket.service.DeviceService;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class DeviceController {

    private final DeviceService deviceService;

    @PostMapping("/history")
    public ResponseEntity<HistoryResponseDTO> getHistory(@RequestBody HistoryRequestDTO request) {
        return ResponseEntity.ok(deviceService.getHistory(request));
    }

    @PostMapping("/prediction")
    public ResponseEntity<HistoryResponseDTO> getPrediction(@RequestBody HistoryRequestDTO request) {
        return ResponseEntity.ok(deviceService.getPrediction(request));
    }

}