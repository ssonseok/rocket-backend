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
import shop.mit301.rocket.dto.Admin_MeasurementReqDTO;
import shop.mit301.rocket.service.Admin_DeviceDataMeasureService;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
// 실시간 데이터 수신은 고성능 처리를 위해 독립된 경로를 사용합니다.
@RequestMapping("/api/measurements")
@RequiredArgsConstructor
@Tag(name = "Measurements", description = "Edge Gateway 실시간 측정값 수신 API")
public class Admin_DeviceDataController {

    // 실시간 측정값 저장을 위한 Service 주입
    private final Admin_DeviceDataMeasureService adminDeviceDataMeasureService;

    /**
     * POST /api/measurements
     * Edge Gateway로부터 실시간 측정 데이터를 수신하여 DB에 저장합니다.
     * MeasurementReqDTO는 deviceSerialNumber와 List<Double> values를 포함합니다.
     */
    @Operation(summary = "실시간 측정 데이터 수신 및 저장",
            description = "Edge Gateway에서 전송된 장비의 센서 측정값 리스트를 받아 저장합니다.")
    @PostMapping
    public ResponseEntity<Map<String, String>> saveMeasurements(@RequestBody Admin_MeasurementReqDTO request) {

        // request.getValues()는 List<Double>이라고 가정
        List<Double> values = request.getValues();
        String serialNumber = request.getDeviceSerialNumber();

        if (values == null || values.isEmpty()) {
            log.warn("측정 데이터 수신 실패: 데이터가 비어 있거나 누락됨. 시리얼: {}", serialNumber);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Collections.singletonMap("message", "측정 데이터(values)가 비어 있을 수 없습니다."));
        }

        try {
            log.info("Receiving {} measurement values from device: {}", values.size(), serialNumber);

            // Service 호출: 측정값 저장 로직 실행
            adminDeviceDataMeasureService.saveMeasurement(serialNumber, values);

            return ResponseEntity.ok(Collections.singletonMap("message", "측정 데이터가 성공적으로 저장되었습니다."));

        } catch (RuntimeException e) {
            log.warn("측정 데이터 저장 실패: 시리얼: {}. 사유: {}", serialNumber, e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Collections.singletonMap("message", "데이터 저장 중 비즈니스 로직 오류 발생: " + e.getMessage()));

        } catch (Exception e) {
            log.error("측정 데이터 저장 중 서버 오류 발생. 시리얼: {}", serialNumber, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.singletonMap("message", "측정 데이터 저장 중 서버 오류 발생."));
        }
    }
}

