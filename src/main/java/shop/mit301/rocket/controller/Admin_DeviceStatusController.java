package shop.mit301.rocket.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import shop.mit301.rocket.dto.Admin_DeviceStatusTestDTO;
import shop.mit301.rocket.service.Admin_DeviceService;

@RestController
@RequestMapping("/api/admin/device")
@RequiredArgsConstructor
public class Admin_DeviceStatusController {

    private final Admin_DeviceService adminDeviceService;

    /**
     * 장치 상태 보기 (연결 테스트 실행) API
     * GET /api/admin/device/{serialNumber}/status
     */
    @GetMapping("/{serialNumber}/status")
    public ResponseEntity<Admin_DeviceStatusTestDTO> getDeviceStatus(@PathVariable String serialNumber) {

        // 1. 서비스 계층의 실시간 통신 로직 호출
        Admin_DeviceStatusTestDTO result = adminDeviceService.getDeviceStatus(serialNumber);

        // 2. DTO 반환 (성공/실패 상태는 DTO 내부에 포함)
        return ResponseEntity.ok(result);
    }
}

