package shop.mit301.rocket.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import shop.mit301.rocket.domain.Device;
import shop.mit301.rocket.domain.DeviceData;
import shop.mit301.rocket.dto.*;
import shop.mit301.rocket.repository.Admin_DeviceRepository;
import shop.mit301.rocket.service.Admin_DeviceRegistrationService;
import shop.mit301.rocket.service.Admin_DeviceService;
import shop.mit301.rocket.websocket.EdgeWebSocketHandler;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/device")
@RequiredArgsConstructor
@Tag(name = "Device", description = "장치 관리 (목록, 상세, 수정, 삭제, 상태보기)")
@Slf4j
public class Admin_DeviceController {

    private final Admin_DeviceService deviceService;

    // -------------------------------------------------------------------------
    // 1. 장치 목록 및 상세 조회
    // -------------------------------------------------------------------------

    @Operation(summary = "장치 목록 조회", description = "등록된 장치들의 목록을 반환합니다.")
    @GetMapping("/list")
    public ResponseEntity<List<Admin_DeviceListDTO>> getDeviceList() {
        List<Admin_DeviceListDTO> deviceList = deviceService.getDeviceList();
        return ResponseEntity.ok(deviceList);
    }

    @Operation(summary = "장치 상세 조회", description = "단일 장치와 연결된 센서 데이터를 조회합니다.")
    @GetMapping("/{deviceSerialNumber}")
    public ResponseEntity<Admin_DeviceDetailRespDTO> getDeviceDetail(
            @PathVariable String deviceSerialNumber) {
        try {
            Admin_DeviceDetailRespDTO detailDTO = deviceService.getDeviceDetail(deviceSerialNumber);
            return ResponseEntity.ok(detailDTO);
        } catch (RuntimeException e) {
            log.warn("장치 상세 조회 실패 - 시리얼: {}. 사유: {}", deviceSerialNumber, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    // -------------------------------------------------------------------------
    // 2. 장치 수정 (Update)
    // -------------------------------------------------------------------------

    @Operation(summary = "장치 정보 및 센서 메타데이터 수정", description = "장치명, Edge 정보, DeviceData 메타데이터를 일괄 수정합니다.")
    @PutMapping("/modify")
    public ResponseEntity<Map<String, String>> modifyDevice(@RequestBody Admin_DeviceModifyReqDTO dto) {
        Map<String, String> response = new HashMap<>();

        try {
            String resultMessage = deviceService.updateFullDeviceInfo(dto);
            response.put("status", "success");
            response.put("message", resultMessage);
            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            response.put("status", "fail");
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);

        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "장치 수정 중 서버 오류 발생.");
            log.error("장치 수정 중 서버 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // -------------------------------------------------------------------------
    // 3. 장치 삭제 (Delete)
    // -------------------------------------------------------------------------

    @Operation(summary = "장치 삭제", description = "특정 장치와 연결된 모든 데이터를 삭제합니다. (RESTful 경로 사용)")
    @DeleteMapping("/{deviceSerialNumber}") // 경로 변수 사용으로 변경
    public ResponseEntity<String> deleteDevice(@PathVariable String deviceSerialNumber) {
        try {
            // DTO 생성 불필요하게 바로 Service 호출 가능하도록 로직 변경 (Admin_DeviceDeleteDTO를 서비스 내부에서 처리)
            Admin_DeviceDeleteDTO dto = new Admin_DeviceDeleteDTO();
            dto.setDeviceSerialNumber(deviceSerialNumber);

            String result = deviceService.deleteDevice(dto);
            return ResponseEntity.ok(result);

        } catch (RuntimeException e) {
            log.warn("장치 삭제 실패 - 시리얼: {}. 사유: {}", deviceSerialNumber, e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("삭제 실패: " + e.getMessage());
        } catch (Exception e) {
            log.error("장치 삭제 중 서버 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("삭제 중 서버 오류 발생.");
        }
    }

    // -------------------------------------------------------------------------
    // 4. 장치 상태 및 테스트
    // -------------------------------------------------------------------------

    @Operation(summary = "장비 상태 보기", description = "장치명, 시리얼넘버, 통신 테스트 결과(응답 데이터, 속도)를 조회합니다.")
    @GetMapping("/status/{serialNumber}")
    public ResponseEntity<Admin_DeviceStatusRespDTO> getDeviceStatus(@PathVariable String serialNumber) {
        try {
            Admin_DeviceStatusRespDTO status = deviceService.getDeviceStatus(serialNumber);
            return ResponseEntity.ok(status);
        } catch (RuntimeException e) {
            log.warn("장치 상태 조회 실패 - 시리얼: {}. 사유: {}", serialNumber, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @Operation(summary = "장비 시리얼 중복 체크", description = "장비 등록 전 시리얼 넘버의 중복 여부를 확인합니다.")
    @GetMapping("/check/{serialNumber}")
    public ResponseEntity<Boolean> checkDuplicateSerialNumber(@PathVariable String serialNumber) {
        // true: 중복됨 (사용 불가), false: 중복 아님 (사용 가능)
        boolean isDuplicate = deviceService.checkDuplicateSerialNumber(serialNumber);
        return ResponseEntity.ok(isDuplicate);
    }
}