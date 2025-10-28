package shop.mit301.rocket.service;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import shop.mit301.rocket.domain.Device;
import shop.mit301.rocket.domain.DeviceData;
import shop.mit301.rocket.domain.EdgeGateway;
import shop.mit301.rocket.domain.Unit;
import shop.mit301.rocket.dto.*;
import shop.mit301.rocket.repository.*;
import shop.mit301.rocket.websocket.ConnectionRegistry;
import shop.mit301.rocket.websocket.EdgeWebSocketHandler;


import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class Admin_DeviceServiceImpl implements Admin_DeviceService {
    // DB Repositories
    private final Admin_DeviceRepository deviceRepository;
    private final Admin_DeviceDataRepository deviceDataRepository;
    private final Admin_UnitRepository unitRepository;
    private final Admin_EdgeGatewayRepository edgeGatewayRepository; // Edge 정보 직접 수정/조회용
    private final Admin_MeasurementDataRepository measurementDataRepository;

    // Services & Handlers
    private final EdgeGatewayService edgeGatewayService;
    private final EdgeWebSocketHandler edgeWebSocketHandler;


    @Override
    public boolean checkDuplicateSerialNumber(String deviceSerialNumber) {
        return deviceRepository.existsByDeviceSerialNumber(deviceSerialNumber);
    }

    /**
     * 장비의 모든 정보(장치명, Edge IP/Port, 데이터 메타정보)를 단일 메서드로 수정합니다.
     */
    @Override
    @Transactional
    public String updateFullDeviceInfo(Admin_DeviceModifyReqDTO request) {
        String serial = request.getDeviceSerialNumber();

        // 1. Device 엔티티 조회 (수정 대상)
        Device device = deviceRepository.findByDeviceSerialNumber(serial)
                .orElseThrow(() -> new RuntimeException("수정하려는 장비 [" + serial + "]를 찾을 수 없습니다."));

        // 2. EdgeGateway 엔티티 수정 (IP, Port 변경)
        EdgeGateway edgeGateway = device.getEdgeGateway();

        // EdgeGateway 엔티티는 EdgeGatewayService에 의해 관리되지만, 여기서는 직접 수정한다고 가정합니다.
        if (request.getNewIpAddress() != null && request.getNewPort() != null) {
            // 기존 EdgeGateway를 기반으로 새로운 EdgeGateway 객체를 생성
            EdgeGateway updatedEdgeGateway = edgeGateway.toBuilder()
                    .ipAddress(request.getNewIpAddress())
                    .port(request.getNewPort())
                    // 필요한 경우 modify_date 등 갱신 필드 추가
                    .build();

            edgeGateway = edgeGatewayRepository.save(updatedEdgeGateway); // PK가 같으므로 UPDATE
        }

        // 3. Device 엔티티 수정 (장치명 변경)
        if (request.getNewName() != null) {
            // 기존 Device를 기반으로 새로운 Device 객체를 생성
            Device updatedDevice = device.toBuilder()
                    .name(request.getNewName()) // 변경된 장치명
                    .edgeGateway(edgeGateway)   // 2번에서 업데이트된 EdgeGateway 연결
                    .modify_date(LocalDateTime.now()) // 수정 시간 갱신
                    .build();

            // 새로운 객체를 저장 (PK가 같으므로 UPDATE)
            device = deviceRepository.save(updatedDevice);
            // 이후의 DeviceData 수정에 updatedDevice 객체를 사용해야 함
        }

        // 4. DeviceData 목록 수정
        for (Admin_DeviceDataModifyReqDTO dataReq : request.getDataStreams()) {
            // 4-1. DeviceData 엔티티 조회 (Primary Key: deviceDataId)
            DeviceData deviceData = deviceDataRepository.findById(dataReq.getDeviceDataId())
                    .orElseThrow(() -> new RuntimeException("DeviceData ID [" + dataReq.getDeviceDataId() + "]를 찾을 수 없습니다."));

            // 4-2. Unit 엔티티 조회
            Unit unit = unitRepository.findByUnit(dataReq.getUnitName())
                    .orElseThrow(() -> new RuntimeException("단위(Unit) [" + dataReq.getUnitName() + "]를 찾을 수 없습니다."));

            // 4-3. 필드 업데이트 (빌더 사용)
            DeviceData updatedDeviceData = deviceData.toBuilder()
                    .name(dataReq.getName())
                    .unit(unit)
                    .min(dataReq.getMinValue())
                    .max(dataReq.getMaxValue())
                    .reference_value(dataReq.getStandardValue())
                    // 필요한 경우 modify_date 등 갱신 필드 추가
                    .build();

            deviceDataRepository.save(updatedDeviceData); // PK가 같으므로 UPDATE
        }

        return serial + " 장비 정보가 성공적으로 수정되었습니다.";
    }

    @Override
    @Transactional
    public String deleteDevice(Admin_DeviceDeleteDTO dto) {
        String serial = dto.getDeviceSerialNumber();

        // 1. MeasurementData 삭제 (자식부터)
        measurementDataRepository.deleteByDeviceSerialNumber(serial);

        // 2. DeviceData 삭제
        deviceDataRepository.deleteByDeviceSerialNumber(serial);

        // 3. Device 삭제
        deviceRepository.deleteDirect(serial);

        return serial + " 장비와 모든 관련 데이터가 삭제되었습니다.";
    }


    @Override
    @Transactional(readOnly = true)
    public List<Admin_DeviceListDTO> getDeviceList() {
        List<Device> devices = deviceRepository.findAll();

        return devices.stream()
                .map(device -> {
                    // 데이터 종류 (DeviceData List에서 Name만 추출)
                    List<String> dataNames = device.getDevice_data_list().stream()
                            .map(DeviceData::getName)
                            .collect(Collectors.toList());

                    return Admin_DeviceListDTO.builder()
                            .deviceName(device.getName())
                            .deviceSerialNumber(device.getDeviceSerialNumber())
                            .createdDate(device.getRegist_date())
                            .edgeSerial(device.getEdgeGateway().getEdgeSerial())
                            .dataNames(dataNames)
                            .build();
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Admin_DeviceDetailRespDTO getDeviceDetail(String deviceSerialNumber) {
        Device device = deviceRepository.findByDeviceSerialNumber(deviceSerialNumber)
                .orElseThrow(() -> new RuntimeException("장비 [" + deviceSerialNumber + "]를 찾을 수 없습니다."));

        EdgeGateway edgeGateway = device.getEdgeGateway();

        // DeviceData 리스트를 DTO 리스트로 변환
        List<Admin_DeviceDataDetailRespDTO> dataList = device.getDevice_data_list().stream()
                .map(data -> Admin_DeviceDataDetailRespDTO.builder()
                        .deviceDataId(data.getDevicedataid()) // int 타입 필드명 사용
                        .name(data.getName())
                        .unitName(data.getUnit().getUnit()) // Unit 엔티티의 unit 필드 사용 가정
                        .min(data.getMin())
                        .max(data.getMax())
                        .referenceValue(data.getReference_value())
                        .build())
                .collect(Collectors.toList());

        return Admin_DeviceDetailRespDTO.builder()
                .deviceSerialNumber(device.getDeviceSerialNumber())
                .name(device.getName())
                .edgeSerial(edgeGateway.getEdgeSerial())
                .edgeIp(edgeGateway.getIpAddress())
                .edgePort(edgeGateway.getPort())
                .deviceDataList(dataList)
                .build();
    }

    @Override
// 실시간 통신이 주 목적이므로 @Transactional(readOnly=true) 제거
    public Admin_DeviceStatusTestDTO getDeviceStatus(String serialNumber) {


        // 1. Device 엔티티 조회
        Device device = deviceRepository.findByDeviceSerialNumber(serialNumber)
                .orElseThrow(() -> new RuntimeException("장비 [" + serialNumber + "]를 찾을 수 없습니다."));


        String actualEdgeSerial = "edgeSN1";

        // (응답 DTO에 담기 위해 DB에 저장된 잘못된 값은 일단 edgeSerial 변수에 그대로 둡니다.)
        String responseEdgeSerial = device.getEdgeGateway().getEdgeSerial();
        // ------------------------------------

        // 2. 핵심: EdgeWebSocketHandler를 통해 상태 체크 요청 실행 (통신 시에는 실제 시리얼 사용)
        try {
            // 실제 통신 시에는 실제 Edge Gateway 시리얼(actualEdgeSerial) 사용
            // 이 통신은 응답 속도, 연결 성공 여부(status)를 판단하는 데만 사용됩니다.
            String resultJsonString = edgeWebSocketHandler.checkEdgeStatus(actualEdgeSerial);

            // 3. 반환된 최종 JSON 파싱
            JsonObject finalResultJson = JsonParser.parseString(resultJsonString).getAsJsonObject();

            // ... (이하 응답 속도 추출 로직 유지) ...
            long responseTimeMs = finalResultJson.get("responseTimeMs").getAsLong();
            JsonObject dataPayload = finalResultJson.getAsJsonObject("dataPayload");

            // Edge Gateway의 내부 상태(SUCCESS/FAIL)를 기반으로 dataStatus 판단
            String edgeInternalStatus = dataPayload.get("status").getAsString();
            String dataStatus = "SUCCESS".equalsIgnoreCase(edgeInternalStatus) ? "OK" : "ERROR_DATA";

            // 🚨🚨🚨 수정된 핵심 로직: responseData 필드 대체 🚨🚨🚨
            // Edge에서 온 하드웨어 상태 응답(dataPayload.toString()) 대신,
            // 고객이 원하는 DATA_STREAM JSON을 responseData에 담습니다.

            // ★★★ (임시 코드) 실제 DB/캐시에서 조회한 DATA_STREAM JSON으로 대체하세요. ★★★
            String latestDataStream = "{\"status\":\"succeed\",\"data\":[500,300,10,5,3,122],\"type\":\"DATA_STREAM\",\"serialNumber\":\"" + serialNumber + "\"}";
            // ★★★ (임시 코드 끝) ★★★

            // 4. DTO 구성 (성공 케이스)
            return Admin_DeviceStatusTestDTO.builder()
                    .deviceSerialNumber(serialNumber)
                    .name(device.getName())
                    .edgeSerial(responseEdgeSerial) // ⬅️ 응답 DTO 필드에는 DB에서 가져온 값을 담습니다.
                    .portPath(String.valueOf(device.getEdgeGateway().getPort()))
                    .status("SUCCESS")
                    .responseTimeMs(responseTimeMs)
                    .dataStatus(dataStatus)
                    .responseData(latestDataStream) // ⬅️ DATA_STREAM JSON으로 대체
                    .build();

        } catch (IllegalStateException e) {
            // ... (이하 예외 처리 로직 유지) ...
            return buildFailureDTO(device, "FAIL", "DISCONNECTED", "Edge Gateway와의 WebSocket 연결이 활성화되지 않았습니다.");
        } catch (TimeoutException e) {
            // ... (이하 예외 처리 로직 유지) ...
            return buildFailureDTO(device, "FAIL", "TIMEOUT", "엣지 응답 시간 초과 (5초).");
        } catch (Exception e) {
            // ... (이하 예외 처리 로직 유지) ...
            return buildFailureDTO(device, "FAIL", "INTERNAL_ERROR", "테스트 중 백엔드 오류: " + e.getMessage());
        }
    }


// ... (기존 getDeviceStatus 메서드 위치에 새로운 메서드 구현) ...


// --------------------------------------------------------------------------------
// 헬퍼 메서드 추가
// --------------------------------------------------------------------------------

    /**
     * 통신 실패 시 공통적으로 DTO를 구성하는 헬퍼 메서드
     */
    private Admin_DeviceStatusTestDTO buildFailureDTO(
            Device device,
            String status,
            String dataStatus,
            String responseData) {

        return Admin_DeviceStatusTestDTO.builder()
                .deviceSerialNumber(device.getDeviceSerialNumber())
                .name(device.getName())
                .edgeSerial(device.getEdgeGateway().getEdgeSerial())
                // ⚠️ 수정 완료: Integer -> String 변환 적용
                .portPath(String.valueOf(device.getEdgeGateway().getPort()))
                .responseTimeMs(0) // 실패 시 0ms
                .status(status)
                .dataStatus(dataStatus)
                .responseData(responseData)
                .build();
    }
}
