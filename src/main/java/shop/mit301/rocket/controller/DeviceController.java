package shop.mit301.rocket.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import shop.mit301.rocket.dto.HistoryRequestDTO;
import shop.mit301.rocket.dto.HistoryResponseDTO;
import shop.mit301.rocket.dto.SensorResponseDTO;
import shop.mit301.rocket.service.DeviceService;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

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

    @GetMapping("/sensors")
    public List<SensorResponseDTO> getSensors(@RequestParam(required = false) String sensorIds) {
        if (sensorIds == null || sensorIds.isEmpty()) {
            // sensorIds가 없으면 전체 센서 목록 반환
            return deviceService.getAllSensors();
        }

        List<Integer> ids = Arrays.stream(sensorIds.split(","))
                .map(Integer::parseInt)
                .collect(Collectors.toList());
        return deviceService.collectAndSend(ids);
    }

}