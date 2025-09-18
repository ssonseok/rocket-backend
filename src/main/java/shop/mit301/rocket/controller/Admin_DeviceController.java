package shop.mit301.rocket.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import shop.mit301.rocket.dto.Admin_DeviceRegisterReqDTO;
import shop.mit301.rocket.dto.Admin_DeviceRegisterRespDTO;
import shop.mit301.rocket.service.Admin_DeviceService;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/device")
@RequiredArgsConstructor
@Tag(name = "Device", description = "장비 관련 API")

public class Admin_DeviceController {

    private final Admin_DeviceService adminDeviceService;

    @Operation(
            summary = "장치 등록",
            description = "장치 정보와 포트를 입력받아 장치를 등록하고 센서 정보를 자동 생성합니다. 테스트 통신 성공 여부를 반환합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "등록 성공",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "400", description = "등록 실패 (중복 시)",
                    content = @Content(mediaType = "application/json"))
    })

    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> registerDevice(
            @Parameter(description = "장치 등록 요청 DTO", required = true)
            @RequestBody Admin_DeviceRegisterReqDTO request) {

        Admin_DeviceRegisterRespDTO resp = adminDeviceService.registerDevice(request);

        Map<String, Object> response = new HashMap<>();
        if (resp.isTestSuccess()) {
            response.put("status", "success");
            response.put("errorType", "none");
            response.put("device", resp);
            return ResponseEntity.ok(response);
        } else {
            response.put("status", "fail");
            response.put("errorType", "duplicateSN");
            response.put("device", resp);
            return ResponseEntity.badRequest().body(response);
        }
    }
}
