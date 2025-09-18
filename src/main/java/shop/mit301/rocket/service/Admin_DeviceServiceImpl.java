package shop.mit301.rocket.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import shop.mit301.rocket.domain.Device;
import shop.mit301.rocket.domain.DeviceData;
import shop.mit301.rocket.dto.Admin_DeviceRegisterReqDTO;
import shop.mit301.rocket.dto.Admin_DeviceRegisterRespDTO;
import shop.mit301.rocket.dto.DeviceDataDTO;
import shop.mit301.rocket.repository.Admin_DeviceDataRepository;
import shop.mit301.rocket.repository.Admin_DeviceRepository;
import shop.mit301.rocket.repository.Admin_UnitRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class Admin_DeviceServiceImpl implements Admin_DeviceService{

    private final Admin_DeviceRepository adminDeviceRepository;
    private final Admin_UnitRepository adminUnitRepository;
    private final Admin_DeviceDataRepository adminDeviceDataRepository;

    @Override
    public boolean checkDuplicateSerialNumber(String deviceSerialNumber) {
        return adminDeviceRepository.existsByDeviceSerialNumber(deviceSerialNumber);
    }

    @Override
    public String testDeviceConnection(String ip, int port) {
        //엣지없어서 항상 연결 성공처리
        //===엣지구현되면 호출할 부분===
        return "success";
    }

    @Override
    public Admin_DeviceRegisterRespDTO registerDevice(Admin_DeviceRegisterReqDTO request) {

        // 1) 시리얼넘버 중복 체크
        if (checkDuplicateSerialNumber(request.getDeviceSerialNumber())) {
            return Admin_DeviceRegisterRespDTO.builder()
                    .deviceSerialNumber(request.getDeviceSerialNumber())
                    .name(request.getName())
                    .ip(request.getIp())
                    .port(request.getPort())
                    .testSuccess(false)      // 실패 처리
                    .sensors(Collections.emptyList())
                    .build();
        }

        // 2) Device 저장
        Device device = Device.builder()
                .deviceSerialNumber(request.getDeviceSerialNumber())
                .name(request.getName())
                .ip(request.getIp())
                .port(request.getPort())
                .regist_date(LocalDateTime.now())
                .build();
        adminDeviceRepository.save(device);

        // 3) DeviceData 자동 생성 (예시 5개 Mock)
        List<DeviceDataDTO> sensors = new ArrayList<>();
        for (int i = 1; i <= 5; i++) {
            DeviceDataDTO sensor = new DeviceDataDTO();
            sensor.setName("Sensor" + i);       // 데이터 명칭
            sensor.setMin(0);
            sensor.setMax(100);
            sensor.setReferenceValue(50);
            sensor.setUnitId(1);                // 기본 단위 ID
            sensors.add(sensor);

            DeviceData deviceData = DeviceData.builder()
                    .device(device)
                    .name(sensor.getName())
                    .min(sensor.getMin())
                    .max(sensor.getMax())
                    .reference_value(sensor.getReferenceValue())
                    .unit(adminUnitRepository.findById(sensor.getUnitId()).orElse(null))
                    .build();
            adminDeviceDataRepository.save(deviceData);
        }

        // 4) Response DTO 생성-
        return Admin_DeviceRegisterRespDTO.builder()
                .deviceSerialNumber(device.getDeviceSerialNumber())
                .name(device.getName())
                .ip(device.getIp())
                .port(device.getPort())
                .testSuccess(true)  // Mock 연결 성공
                .sensors(sensors)
                .build();
    }
}
