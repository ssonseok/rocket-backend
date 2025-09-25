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
        // 시리얼넘버 중복 체크
        if (checkDuplicateSerialNumber(request.getDeviceSerialNumber())) {
            return Admin_DeviceRegisterRespDTO.builder()
                    .deviceSerialNumber(request.getDeviceSerialNumber())
                    .name(request.getName())
                    .ip(request.getIp())
                    .port(request.getPort())
                    .testSuccess(false)
                    .sensors(Collections.emptyList())
                    .build();
        }

        // Device 저장
        Device device = Device.builder()
                .deviceSerialNumber(request.getDeviceSerialNumber())
                .name(request.getName())
                .ip(request.getIp())
                .port(request.getPort())
                .regist_date(LocalDateTime.now())
                .build();
        adminDeviceRepository.save(device);

        // 초기에는 장치데이터 없음 (엣지에서 수신 후 UI에서 입력)
        return Admin_DeviceRegisterRespDTO.builder()
                .deviceSerialNumber(device.getDeviceSerialNumber())
                .name(device.getName())
                .ip(device.getIp())
                .port(device.getPort())
                .testSuccess(true)
                .sensors(Collections.emptyList())
                .build();
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
        String serial = dto.getDeviceSerialNumber();

        //연결된 장치 데이터 먼저 삭제
        adminDeviceDataRepository.deleteByDevice_DeviceSerialNumber(serial);

        Device device = adminDeviceRepository.findById(serial).get();
        adminDeviceRepository.delete(device);

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


}
