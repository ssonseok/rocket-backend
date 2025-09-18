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
@Tag(name = "Device", description = "장치 관련 API")
public class Admin_DeviceController {

    private final Admin_DeviceService deviceService;

    @Operation(summary = "장치 등록", description = "장치 정보 입력 후 등록. 성공하면 장치 데이터 입력 UI 활성화")
    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> registerDevice(
            @RequestBody Admin_DeviceRegisterReqDTO request) {

        Admin_DeviceRegisterRespDTO resp = deviceService.registerDevice(request);

        Map<String, Object> response = new HashMap<>();
        if (resp.isTestSuccess()) {
            response.put("status", "success");
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
