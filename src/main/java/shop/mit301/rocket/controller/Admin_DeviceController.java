package shop.mit301.rocket.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import shop.mit301.rocket.domain.Device;
import shop.mit301.rocket.domain.DeviceData;
import shop.mit301.rocket.dto.*;
import shop.mit301.rocket.repository.Admin_DeviceRepository;
import shop.mit301.rocket.service.Admin_DeviceDataService;
import shop.mit301.rocket.service.Admin_DeviceService;
import shop.mit301.rocket.websocket.EdgeWebSocketHandler;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/device")
@RequiredArgsConstructor
@Tag(name = "Device", description = "장치 관련 API")
public class Admin_DeviceController {

    private final Admin_DeviceService deviceService;
    private final EdgeWebSocketHandler edgeHandler;
    private final Admin_DeviceDataService deviceDataService;
    private final Admin_DeviceRepository deviceRepository;


    @Operation(summary = "장치 등록 및 데이터 확보", description = "장치 등록 후 엣지 연결 확인. 데이터가 없으면 대기 상태(pending) 반환.")
    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> registerDevice(@RequestBody Admin_DeviceRegisterReqDTO request) {
        // 1. 장치 정보 DB에 등록
        Admin_DeviceRegisterRespDTO resp = deviceService.registerDevice(request);
        Map<String, Object> map = new HashMap<>();

        // 2. 등록 실패 (시리얼 넘버 중복) 처리
        if (!resp.isTestSuccess()) {
            map.put("status", "fail");
            map.put("errorType", "duplicateSN");
            return ResponseEntity.badRequest().body(map);
        }

        // 3. 엣지 연결 확인
        if (!edgeHandler.isConnected(request.getDeviceSerialNumber())) {
            map.put("status", "fail");
            map.put("errorType", "edgeNotConnected");
            return ResponseEntity.badRequest().body(map);
        }

        // 4. DB에서 DeviceData 조회 (엣지 데이터 수신 여부 확인)
        List<DeviceData> deviceDataList = deviceDataService.getDeviceDataList(resp.getDeviceSerialNumber());

        // ✨ 5. DeviceData가 없는 경우 (엣지가 아직 데이터를 보내지 않은 경우)
        if (deviceDataList.isEmpty()) {
            // UI에게 "연결은 성공했으나 데이터가 아직 준비되지 않았으니 대기하라"고 전달
            map.put("status", "pending");
            map.put("message", "엣지 연결 성공. 센서 데이터 수신을 위해 잠시 기다려주세요.");
            map.put("device", resp);
            return ResponseEntity.ok(map);
        }

        // 6. DeviceData가 있는 경우 (폼 생성 데이터 확보 완료)

        // DeviceData → DeviceDataDTO 변환
        List<DeviceDataDTO> sensors = deviceDataList.stream()
                .map(dd -> DeviceDataDTO.builder()
                        .name(dd.getName())
                        .min(dd.getMin())
                        .max(dd.getMax())
                        .referenceValue(dd.getReference_value())
                        // Unit 엔티티가 null일 가능성을 대비하여 null 체크 로직 추가 권장
                        .unitId(dd.getUnit() != null ? dd.getUnit().getUnitid() : 0)
                        .build())
                .collect(Collectors.toList());

        // DTO에 세팅
        resp.setSensors(sensors);
        resp.setDataCount(sensors.size());

        map.put("status", "success");
        map.put("device", resp);
        return ResponseEntity.ok(map);
    }

    @Operation(summary = "등록된 장치의 센서 데이터 현황 확인", description = "이미 등록된 장치의 DeviceData 목록(센서 개수)을 조회하고 반환합니다.")
    @GetMapping("/{serialNumber}/data-status")
    public ResponseEntity<Map<String, Object>> getDataStatus(@PathVariable String serialNumber) {
        Map<String, Object> map = new HashMap<>();

        // 1. 장치가 DB에 있는지 확인
        Device device = deviceRepository.findById(serialNumber).orElse(null);
        if (device == null) {
            map.put("status", "fail");
            map.put("errorType", "deviceNotFound");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(map);
        }

        // 2. DeviceData 조회
        List<DeviceData> deviceDataList = deviceDataService.getDeviceDataList(serialNumber);

        if (deviceDataList.isEmpty()) {
            // 데이터가 아직 생성되지 않은 상태
            map.put("status", "pending");
            map.put("message", "엣지 연결 성공. 데이터 수신 대기 중.");
            map.put("dataCount", 0);
            return ResponseEntity.ok(map);
        }

        // 3. 데이터 확보 완료 (폼 생성 가능)
        List<Admin_DeviceDataRegisterRespDTO> sensors = deviceDataList.stream()
                .map(dd -> Admin_DeviceDataRegisterRespDTO.builder()
                        .name(dd.getName())
                        .min(dd.getMin())
                        .max(dd.getMax())
                        .referenceValue(dd.getReference_value())
                        .unitId(dd.getUnit() != null ? dd.getUnit().getUnitid() : 0)
                        .build())
                .collect(Collectors.toList());

        map.put("status", "success");
        map.put("dataCount", sensors.size());
        map.put("sensors", sensors);
        return ResponseEntity.ok(map);
    }


    @Operation(
            summary = "장치 목록 조회",
            description = "등록된 장치들의 목록을 반환. 장치명, 시리얼번호, 등록일, 데이터 종류 리스트 포함"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Admin_DeviceListDTO.class)))
    })
    @GetMapping("/list")
    public ResponseEntity<List<Admin_DeviceListDTO>> getDeviceList() {
        List<Admin_DeviceListDTO> deviceList = deviceService.getDeviceList();
        return ResponseEntity.ok(deviceList);
    }

    @Operation(
            summary = "장치 삭제",
            description = "특정 장치를 삭제합니다",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "삭제할 장치 정보",
                    required = true,
                    content = @Content(schema = @Schema(implementation = Admin_DeviceDeleteDTO.class))
            )
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "삭제 성공"),
            @ApiResponse(responseCode = "400", description = "삭제 실패")
    })
    @DeleteMapping("/delete")
    public ResponseEntity<String> deleteDevice(@RequestParam String deviceSerialNumber) {
        try {
            Admin_DeviceDeleteDTO dto = new Admin_DeviceDeleteDTO();
            dto.setDeviceSerialNumber(deviceSerialNumber);

            String result = deviceService.deleteDevice(dto);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("삭제 실패: " + e.getMessage());
        }
    }

    // 장치 수정
    @Operation(summary = "장치 수정", description = "특정 장치 정보를 수정합니다. IP/Port 테스트 성공 시만 수정 가능")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "수정 성공"),
            @ApiResponse(responseCode = "200", description = "수정 실패 (테스트 실패 시)")
    })
    @PutMapping("/modify")
    public ResponseEntity<Map<String, String>> modifyDevice(@RequestBody Admin_DeviceModifyReqDTO dto) {
        String result = deviceService.modifyDevice(dto);

        Map<String, String> response = new HashMap<>();
        response.put("status", result);

        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "장치 상세 조회",
            description = "단일 장치와 연결된 센서 데이터를 조회합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Admin_DeviceDetailDTO.class))),
            @ApiResponse(responseCode = "404", description = "장치 없음")
    })
    @GetMapping("/{deviceSerialNumber}")
    public ResponseEntity<Admin_DeviceDetailDTO> getDeviceDetail(
            @PathVariable String deviceSerialNumber) {

        Admin_DeviceDetailDTO detailDTO = deviceService.getDeviceDetail(deviceSerialNumber);
        return ResponseEntity.ok(detailDTO);
    }

    // =========================================================================
    // ✅ 1021: 상태 보기 및 테스트 기능 추가 영역
    // =========================================================================

    @Operation(summary = "장비 상태 보기 기본 정보", description = "상태 화면에 필요한 장치명, 시리얼넘버 등 기본 정보를 조회합니다.")
    @GetMapping("/status/{serialNumber}")
    public ResponseEntity<Admin_DeviceStatusRespDTO> getDeviceStatus(@PathVariable String serialNumber) {
        // Service에서 Device 정보를 조회하여 DTO를 반환
        Admin_DeviceStatusRespDTO status = deviceService.getDeviceStatus(serialNumber);
        return ResponseEntity.ok(status);
    }

    @Operation(summary = "장치 연결 테스트 실행", description = "엣지 장치와의 WebSocket 통신을 통해 연결 및 데이터 수신 테스트를 실행합니다. 결과는 'success' 또는 'fail'만 반환합니다.")
    @GetMapping("/test/{serialNumber}")
    public ResponseEntity<Map<String, String>> testDeviceConnection(@PathVariable String serialNumber) {
        Map<String, String> response = new HashMap<>();
        try {
            // Service는 테스트 후 캐시에 상세 결과를 저장하고, "success" 또는 "fail" 문자열만 반환
            String result = deviceService.testDeviceConnection(serialNumber);

            response.put("status", result);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            // 통신 오류, 타임아웃, 장치 없음 등 모든 예외 처리
            // UI에 fail을 반환하고 상세 메시지를 포함 (옵션)
            response.put("status", "fail");
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @Operation(summary = "최근 테스트 결과 상세 조회", description = "테스트 성공 후, Service의 캐시에 저장된 상세 테스트 결과 DTO를 조회합니다.")
    @GetMapping("/test/result/{serialNumber}")
    public ResponseEntity<Admin_DeviceStatusTestDTO> getLatestTestResult(@PathVariable String serialNumber) {
        // Service에서 캐시된 DTO를 가져와 UI에 상세 정보를 제공합니다.
        // 캐시된 결과가 없으면 Service에서 RuntimeException을 던질 수 있습니다.
        Admin_DeviceStatusTestDTO resultDTO = deviceService.getLatestTestResult(serialNumber);
        return ResponseEntity.ok(resultDTO);
    }
    // =========================================================================
    // ✅ 1021: 상태 보기 및 테스트 기능 추가 영역 끝
    // =========================================================================
}



