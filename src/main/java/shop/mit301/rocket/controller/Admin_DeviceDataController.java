package shop.mit301.rocket.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import shop.mit301.rocket.domain.DeviceData;
import shop.mit301.rocket.dto.Admin_DeviceDataDTO;
import shop.mit301.rocket.dto.Admin_DeviceDataMeasureDTO;
import shop.mit301.rocket.dto.Admin_DeviceDataRegisterReqDTO;
import shop.mit301.rocket.dto.Admin_DeviceDataRegisterRespDTO;
import shop.mit301.rocket.service.Admin_DeviceDataMeasureService;
import shop.mit301.rocket.service.Admin_DeviceDataService;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/device/{deviceSerialNumber}/deviceData")
@RequiredArgsConstructor
@Tag(name = "DeviceData", description = "장치 데이터 관련 API")
public class Admin_DeviceDataController {

    private final Admin_DeviceDataService deviceDataService;
    private final Admin_DeviceDataMeasureService adminDeviceDataMeasureService;

    @Operation(summary = "장치 데이터 등록", description = "UI에서 입력한 데이터(min/max/ref/unit) 값 등록")
    @PostMapping("/register")
    public List<Admin_DeviceDataRegisterRespDTO> registerDeviceData(
            @PathVariable String deviceSerialNumber,
            @RequestBody List<Admin_DeviceDataRegisterReqDTO> requestList) {
        return deviceDataService.registerDeviceData(deviceSerialNumber, requestList);
    }

    @Operation(summary = "장치 데이터 조회", description = "등록된 장치의 데이터 목록 조회")
    @GetMapping("/list")
    public List<DeviceData> getDeviceDataList(@PathVariable String deviceSerialNumber) {
        return deviceDataService.getDeviceDataList(deviceSerialNumber);
    }
    @Operation(
            summary = "측정 데이터 저장 (Edge Gateway 전용)",
            description = "Edge Gateway에서 전송된 장치별 실시간 측정값(values)을 DB에 저장합니다. " +
                    "values의 개수는 사전에 등록된 DeviceData 항목 수와 일치해야 합니다."
    )
    @PostMapping("/measurements") // 예: /api/device/{deviceSerialNumber}/deviceData/measurements
    public ResponseEntity<String> saveMeasurements(
            @PathVariable String deviceSerialNumber,
            @RequestBody Admin_DeviceDataMeasureDTO requestDTO) { // DTO를 통째로 받도록 수정

        try {
            // 💡 [수정] 서비스 호출: DTO에서 values 리스트만 추출하여 전달
            adminDeviceDataMeasureService.saveMeasurement(
                    requestDTO.getDeviceSerialNumber(), // DTO에서 Serial Number 재확보 (PathVariable과 동일)
                    requestDTO.getValues().stream().map(Integer::doubleValue).collect(Collectors.toList()) // Integer -> Double 변환
            );

            return ResponseEntity.ok("success");
        } catch (RuntimeException e) {
            // 센서 개수 불일치 등 런타임 오류 처리
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("fail: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("fail: 측정 데이터 저장 중 오류 발생");
        }
    }
}
