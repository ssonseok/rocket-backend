package shop.mit301.rocket.service;

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

    //장비삭제
    @Override
    @Transactional
    public String deleteDevice(Admin_DeviceDeleteDTO dto) {
        String serial = dto.getDeviceSerialNumber();

        Device device = deviceRepository.findByDeviceSerialNumber(serial)
                .orElseThrow(() -> new RuntimeException("삭제하려는 장비 [" + serial + "]를 찾을 수 없습니다."));

        // 1. DeviceData 조회
        List<DeviceData> deviceDataList = deviceDataRepository.findByDevice_DeviceSerialNumber(serial);

        // 2. MeasurementData 삭제
        for (DeviceData data : deviceDataList) {
            measurementDataRepository.deleteByDevicedata(data);
        }

        // 3. DeviceData 삭제
        deviceDataRepository.deleteAll(deviceDataList);

        // 4. Device 삭제
        deviceRepository.delete(device);

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
    @Transactional(readOnly = true)
    public Admin_DeviceStatusRespDTO getDeviceStatus(String serialNumber) {
        // 1. Device 엔티티 조회
        Device device = deviceRepository.findByDeviceSerialNumber(serialNumber)
                .orElseThrow(() -> new RuntimeException("장비 [" + serialNumber + "]를 찾을 수 없습니다."));

        // 2. Edge의 WebSocket 연결 상태 확인
        String edgeSerial = device.getEdgeGateway().getEdgeSerial();
        boolean isWsConnected = edgeWebSocketHandler.isConnected(edgeSerial);

        // 3. Edge Gateway의 DB 상태 정보
        String dbStatus = device.getEdgeGateway().getStatus();

        // TODO: lastDataReceived 및 responseTimeMs는 MeasurementData 테이블에서 조회하는 로직이 필요함.
        // 현재는 더미 데이터 반환 또는 단순하게 처리

        return Admin_DeviceStatusRespDTO.builder()
                .deviceSerialNumber(serialNumber)
                .deviceName(device.getName())
                .edgeSerial(edgeSerial)
                .wsConnected(isWsConnected)
                .dbStatus(dbStatus)
                // 임시 값 또는 DB에서 조회하도록 로직 추가 필요
                .lastDataReceived(LocalDateTime.now())
                .responseTimeMs(0L)
                .build();
    }
}
