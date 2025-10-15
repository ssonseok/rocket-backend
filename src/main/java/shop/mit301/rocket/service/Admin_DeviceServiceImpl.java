package shop.mit301.rocket.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import shop.mit301.rocket.domain.Device;
import shop.mit301.rocket.domain.DeviceData;
import shop.mit301.rocket.dto.*;
import shop.mit301.rocket.repository.Admin_DeviceDataRepository;
import shop.mit301.rocket.repository.Admin_DeviceRepository;
import shop.mit301.rocket.repository.Admin_UnitRepository;
import shop.mit301.rocket.websocket.ConnectionRegistry;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class Admin_DeviceServiceImpl implements Admin_DeviceService {

    private final Admin_DeviceRepository adminDeviceRepository;
    private final Admin_UnitRepository adminUnitRepository;
    private final Admin_DeviceDataRepository adminDeviceDataRepository;
    private final ModelMapper modelMapper;
    private final ConnectionRegistry connectionRegistry;

    @Override
    public boolean checkDuplicateSerialNumber(String deviceSerialNumber) {
        return adminDeviceRepository.existsByDeviceSerialNumber(deviceSerialNumber);
    }



    @Override
    @Transactional
    public Admin_DeviceRegisterRespDTO registerDevice(Admin_DeviceRegisterReqDTO request) {
        if (checkDuplicateSerialNumber(request.getDeviceSerialNumber())) {
            return Admin_DeviceRegisterRespDTO.builder()
                    .deviceSerialNumber(request.getDeviceSerialNumber())
                    .name(request.getName())
                    .ip(request.getIp())
                    .port(request.getPort())
                    .testSuccess(false)
                    .sensors(Collections.emptyList())
                    .dataCount(0) // 중복이면 센서 없음
                    .build();
        }

        // 장치 등록
        Device device = Device.builder()
                .deviceSerialNumber(request.getDeviceSerialNumber())
                .name(request.getName())
                .ip(request.getIp())
                .port(request.getPort())
                .regist_date(LocalDateTime.now())
                .build();
        adminDeviceRepository.save(device);

        // 등록 시점에는 DeviceData가 없으므로 0으로 설정
        List<DeviceData> deviceDataList = adminDeviceDataRepository.findByDevice_DeviceSerialNumber(device.getDeviceSerialNumber());
        int sensorCount = deviceDataList.size();

        return Admin_DeviceRegisterRespDTO.builder()
                .deviceSerialNumber(device.getDeviceSerialNumber())
                .name(device.getName())
                .ip(device.getIp())
                .port(device.getPort())
                .testSuccess(true)
                .dataCount(sensorCount) // 0이면 프론트에서 폼 생성 안함
                .build();
    }

    @Override
    public Device getDevice(String serialNumber) {
        return adminDeviceRepository.findById(serialNumber)
                .orElseThrow(() -> new RuntimeException("Device 없음: " + serialNumber));
    }

    @Override
    public List<Admin_DeviceListDTO> getDeviceList() {
        List<Device> devices = adminDeviceRepository.findAll();

        return devices.stream().map(device -> {
            Admin_DeviceListDTO dto = new Admin_DeviceListDTO();
            dto.setDeviceSerialNumber(device.getDeviceSerialNumber());
            dto.setDeviceName(device.getName());
            dto.setCreatedDate(device.getRegist_date());

            // DeviceData에서 name만 추출
            List<String> dataNames = device.getDevice_data_list().stream()
                    .map(DeviceData::getName)
                    .collect(Collectors.toList());
            dto.setDataNames(dataNames);

            return dto;
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public String deleteDevice(Admin_DeviceDeleteDTO dto) {
        Device device = adminDeviceRepository.findById(dto.getDeviceSerialNumber())
                .orElseThrow(() -> new RuntimeException("Device not found"));

        // DeviceData와 MeasurementData 리스트를 강제로 fetch
        device.getDevice_data_list().forEach(dd -> {
            dd.getMeasurement_data_list().size(); // Lazy 강제 초기화
            dd.getUser_device_data_list().size();
        });

        adminDeviceRepository.delete(device); // Cascade로 하위 데이터까지 삭제
        return "success";
    }

    @Override
    @Transactional
    public String modifyDevice(Admin_DeviceModifyReqDTO dto) {
        Device existing = adminDeviceRepository.findById(dto.getDeviceSerialNumber()).get();

        if (!"success".equals(testDeviceConnection(dto.getIp(), dto.getPort()))) {
            return "fail";
        }

        Device updated = Device.builder()
                .deviceSerialNumber(existing.getDeviceSerialNumber())
                .name(dto.getName())
                .ip(dto.getIp())
                .port(dto.getPort())
                .regist_date(existing.getRegist_date())
                .build();

        adminDeviceRepository.save(updated);

        return "success";
    }

    @Override
    public Admin_DeviceStatusRespDTO getDeviceStatus(String serialNumber) {
        return null;
    }

    @Override
    public Admin_DeviceStatusTestDTO testDeviceConnection(String serialNumber) {
        return null;
    }

    @Override
    public Admin_DeviceDetailDTO getDeviceDetail(String deviceSerialNumber) {

        // 1. 장치 조회
        Device device = adminDeviceRepository.findById(deviceSerialNumber)
                .orElseThrow(() -> new RuntimeException("해당 장치가 존재하지 않습니다."));

        // 2. 장치에 연결된 센서 데이터 조회
        List<DeviceData> dataList = adminDeviceDataRepository.findByDevice_DeviceSerialNumber(deviceSerialNumber);

        // 3. 센서 DTO 변환 (ModelMapper + 빌더)
        List<Admin_DeviceDataRegisterRespDTO> sensors = dataList.stream()
                .map(data -> Admin_DeviceDataRegisterRespDTO.builder()
                        .name(data.getName())
                        .min(data.getMin())
                        .max(data.getMax())
                        .referenceValue(data.getReference_value())   // 필드 이름에 맞게 getter 호출
                        .unitId(data.getUnit().getUnitid())          // Unit 객체에서 id 꺼내기
                        .saved(true)
                        .build()
                ).collect(Collectors.toList());

        // 4. 장치 DTO 변환 (빌더 사용)
        return Admin_DeviceDetailDTO.builder()
                .deviceSerialNumber(device.getDeviceSerialNumber())
                .name(device.getName())
                .ip(device.getIp())
                .port(device.getPort())
                .deviceDataList(sensors)
                .build();
    }
    @Override
    public String testDeviceConnection(String ip, int port) {
//        // 최대 대기 시간(ms)
//        final long TIMEOUT_MS = 5000;
//        final long POLL_INTERVAL_MS = 100;
//
//        long startTime = System.currentTimeMillis();
//
//        while (System.currentTimeMillis() - startTime < TIMEOUT_MS) {
//            if (connectionRegistry.isConnected(deviceSerialNumber)) {
//                // 엣지와 연결된 세션 존재
//                return "success";
//            }
//            try {
//                Thread.sleep(POLL_INTERVAL_MS);
//            } catch (InterruptedException e) {
//                Thread.currentThread().interrupt();
//                return "fail";
//            }
//        }
//
//        // 타임아웃 후에도 연결 안 됨
//        return "fail";
        return null;
    }
}
