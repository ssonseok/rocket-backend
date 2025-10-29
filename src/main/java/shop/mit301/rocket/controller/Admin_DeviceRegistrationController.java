package shop.mit301.rocket.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import shop.mit301.rocket.domain.Device;
import shop.mit301.rocket.dto.Admin_DeviceConfigFinalizeReqDTO;
import shop.mit301.rocket.dto.Admin_DeviceVerificationReqDTO;
import shop.mit301.rocket.dto.Admin_DeviceVerificationRespDTO;
import shop.mit301.rocket.service.Admin_DeviceRegistrationService;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/device/registration") // 경로 변경: /api/device/registration
@RequiredArgsConstructor
@Tag(name = "Registration", description = "장치 등록 2단계 플로우")
public class Admin_DeviceRegistrationController {

    private final Admin_DeviceRegistrationService adminDeviceRegistrationService;

    // -------------------------------------------------------------------------
    // 1. 등록 Step 1: 연결 검증 및 데이터 스트림 개수 요청
    // -------------------------------------------------------------------------

    @Operation(summary = "등록 1단계: Edge 연결 유효성 검증",
            description = "Edge IP/Port를 이용해 통신을 시도하고, Edge가 제공하는 데이터 스트림(센서) 개수를 반환합니다.")
    @PostMapping("/verify-connection") // 경로 변경
    public ResponseEntity<Map<String, Object>> verifyConnectionAndGetStreamCount(
            @RequestBody Admin_DeviceVerificationReqDTO request) {

        Map<String, Object> response = new HashMap<>();

        try {
            // DTO 필드명에 맞게 getEdgeIp()와 getEdgePort()로 수정
            log.info("Registration Step 1: Verifying connection to IP:{} Port:{}",
                    request.getEdgeIp(), request.getEdgePort());

            Admin_DeviceVerificationRespDTO verificationResult =
                    adminDeviceRegistrationService.verifyConnectionAndGetStreamCount(request);

            response.put("status", "success");
            response.put("data", verificationResult);
            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            log.warn("등록 Step 1 연결 검증 실패: {}", e.getMessage());
            response.put("status", "fail");
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);

        } catch (Exception e) {
            log.error("등록 Step 1 처리 중 서버 오류 발생.", e);
            response.put("status", "error");
            response.put("message", "서버 오류로 인해 연결 검증에 실패했습니다.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // -------------------------------------------------------------------------
    // 2. 등록 Step 2: 최종 등록
    // -------------------------------------------------------------------------

    @Operation(summary = "등록 2단계: 최종 장치 및 센서 메타데이터 등록",
            description = "Edge 연결 검증 후, 설정된 장치명과 DeviceData 정보를 DB에 최종 저장합니다.")
    @PostMapping("/finalize-config") // 경로 변경
    public ResponseEntity<Map<String, String>> registerDeviceAndDataStreams(
            @RequestBody Admin_DeviceConfigFinalizeReqDTO request) {

        Map<String, String> response = new HashMap<>();

        try {
            log.info("Registration Step 2: Finalizing registration for serial: {}", request.getDeviceSerial());

            // **오류 수정**: 두 번째 인수인 deviceSerial을 DTO에서 추출하여 전달합니다.
            Device device = adminDeviceRegistrationService.registerDeviceAndDataStreams(
                    request,
                    request.getDeviceSerial()
            );

            String resultMessage = "장비 등록 완료: " + device.getName();

            response.put("status", "success");
            response.put("message", resultMessage);
            return new ResponseEntity<>(response, HttpStatus.CREATED);

        } catch (RuntimeException e) {
            log.warn("등록 Step 2 최종 등록 실패: {}", e.getMessage());
            response.put("status", "fail");
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);

        } catch (Exception e) {
            log.error("등록 Step 2 처리 중 서버 오류 발생.", e);
            response.put("status", "error");
            response.put("message", "최종 장치 등록 중 서버 오류 발생.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}