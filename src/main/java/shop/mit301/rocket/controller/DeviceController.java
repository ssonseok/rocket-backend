package shop.mit301.rocket.controller;

import com.google.gson.Gson;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import shop.mit301.rocket.domain.User;
import shop.mit301.rocket.domain.UserGraphLayout;
import shop.mit301.rocket.dto.*;
import shop.mit301.rocket.jwt.JwtTokenProvider;
import shop.mit301.rocket.jwt.JwtUtil;
import shop.mit301.rocket.repository.Admin_UserRepository;
import shop.mit301.rocket.repository.MeasurementDataRepository;
import shop.mit301.rocket.service.DeviceService;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class DeviceController {

    private final DeviceService deviceService;
    private final MeasurementDataRepository measurementDataRepository;
    private final JwtUtil jwtUtil;
    private final JwtTokenProvider jwtTokenProvider;
    private final Admin_UserRepository userRepository;

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

    @GetMapping("/sensors/{deviceSerial}/{sensorId}/value")
    public ResponseEntity<Double> getSensorValue(
            @PathVariable String deviceSerial,
            @PathVariable Integer sensorId) {
        return measurementDataRepository
                .findTopByDevicedata_Device_DeviceSerialNumberAndDevicedata_DevicedataidOrderById_MeasurementdateDesc(deviceSerial, sensorId)
                .map(data -> ResponseEntity.ok(data.getMeasurementvalue()))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/alert/email")
    public ResponseEntity<String> sendEmailAlert(
            @RequestBody Map<String, Object> payload,
            @RequestHeader("Authorization") String authHeader) {

        String token = authHeader.replace("Bearer ", "");
        String userEmail = jwtUtil.getEmailFromToken(token);

        // Map에서 유연하게 값 추출
        String sensorName = (String) payload.getOrDefault("name", "Unknown Sensor");
        double currentValue = ((Number) payload.getOrDefault("currentValue", 0)).doubleValue();
        double referenceValue = ((Number) payload.getOrDefault("threshold", 0)).doubleValue();

        deviceService.sendAlert(userEmail, sensorName, currentValue, referenceValue);

        return ResponseEntity.ok("이메일 알림 전송 완료: " + userEmail);
    }

    @GetMapping("/user/graph-layout-save")
    public List<UserGraphLayout> getGraphLayouts(@RequestHeader("Authorization") String authHeader) {
        String userId = jwtUtil.getUserIdFromHeader(authHeader);
        return deviceService.getLayouts(userId);
    }

    @PostMapping("/user/graph-layout-save")
    public ResponseEntity<?> saveGraphLayouts(@RequestHeader("Authorization") String authHeader,
                                              @RequestBody List<GraphLayoutRequest> layouts) {
        String userId = jwtUtil.getUserIdFromHeader(authHeader);

        layouts.forEach(l -> deviceService.saveOrUpdateLayout(userId, l.getDragId(), l.getLeft(), l.getTop(), l.getWidth(), l.getHeight()));

        return ResponseEntity.ok().build();
    }

    @GetMapping("/user/graph-layout")
    public ResponseEntity<?> getGraphLayout(@RequestHeader("Authorization") String token) {
        String userId = jwtTokenProvider.getUserIdFromToken(token);

        return userRepository.findByUserid(userId)
                .map(user -> {
                    String graphData = user.getGraphData();
                    List<?> activeGraphs = graphData != null
                            ? new Gson().fromJson(graphData, List.class)
                            : List.of();

                    return ResponseEntity.ok(Map.of("activeGraphs", activeGraphs));
                })
                .orElse(ResponseEntity.ok(Map.of("activeGraphs", List.of())));
    }

    @PostMapping("/user/graph-layout")
    public ResponseEntity<?> saveGraphLayout(
            @RequestHeader("Authorization") String token,
            @RequestBody Map<String, Object> body) {

        String userId = jwtTokenProvider.getUserIdFromToken(token);
        List<?> activeGraphs = (List<?>) body.get("activeGraphs");
        String json = new Gson().toJson(activeGraphs);

        return userRepository.findByUserid(userId)
                .map(existingUser -> {
                    existingUser.setGraphData(json); // 🔹 다른 필드 건드리지 않음
                    userRepository.save(existingUser);
                    return ResponseEntity.ok().build();
                })
                .orElseGet(() -> {
                    // 유저가 존재하지 않을 경우(예외적)
                    User newUser = User.builder()
                            .userid(userId)
                            .graphData(json)
                            .build();
                    userRepository.save(newUser);
                    return ResponseEntity.ok().build();
                });
    }
}