package shop.mit301.rocket.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import shop.mit301.rocket.dto.*;
import shop.mit301.rocket.service.Admin_DeviceService;

import java.util.HashMap;
import java.util.List;
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
    public ResponseEntity<Map<String, String>> deleteDevice(@RequestBody Admin_DeviceDeleteDTO dto) {
        deviceService.deleteDevice(dto);

        Map<String, String> response = new HashMap<>();
        response.put("status", "success");
        response.put("errorType", "none");

        return ResponseEntity.ok(response);
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
}


