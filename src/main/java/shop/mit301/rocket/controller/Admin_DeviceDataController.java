package shop.mit301.rocket.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import shop.mit301.rocket.dto.Admin_DeviceDataDTO;
import shop.mit301.rocket.dto.Admin_DeviceDataRegisterReqDTO;
import shop.mit301.rocket.dto.Admin_DeviceDataRegisterRespDTO;
import shop.mit301.rocket.service.Admin_DeviceDataService;

import java.util.List;

@RestController
@RequestMapping("/api/device/{deviceSerialNumber}/sensors")
@RequiredArgsConstructor
@Tag(name = "DeviceData", description = "장치 데이터 관련 API")
public class Admin_DeviceDataController {

    private final Admin_DeviceDataService deviceDataService;

    @Operation(summary = "장치 센서 조회", description = "등록된 장치의 센서 목록 조회")
    @GetMapping("/list")
    public List<Admin_DeviceDataDTO> getDeviceDataList(
            @PathVariable String deviceSerialNumber) {
        return deviceDataService.getDeviceDataList(deviceSerialNumber);
    }

    @Operation(summary = "장치 센서 등록", description = "UI에서 입력한 센서(min/max/ref/unit) 값 등록")
    @PostMapping("/register")
    public List<Admin_DeviceDataRegisterRespDTO> registerDeviceData(
            @PathVariable String deviceSerialNumber,
            @RequestBody List<Admin_DeviceDataRegisterReqDTO> requestList) {
        return deviceDataService.registerDeviceData(deviceSerialNumber, requestList);
    }
}
