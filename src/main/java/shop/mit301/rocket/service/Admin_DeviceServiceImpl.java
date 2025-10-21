package shop.mit301.rocket.service;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
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
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class Admin_DeviceServiceImpl implements Admin_DeviceService {

    private final Admin_DeviceRepository adminDeviceRepository;
    private final Admin_UnitRepository adminUnitRepository;
    private final Admin_DeviceDataRepository adminDeviceDataRepository;
    private final ModelMapper modelMapper;
    private final ConnectionRegistry connectionRegistry;

    // 필드 추가: 가장 최근 테스트 결과를 임시 저장할 인메모리 캐시
    private final Map<String, Admin_DeviceStatusTestDTO> testResultCache = new ConcurrentHashMap<>();

// 필드 추가: JSON 파싱을 위한 Gson (이미 EdgeWebSocketHandler에 있으나, 여기에 Gson 대신 JsonParser 사용)
// private final Gson gson = new Gson(); // 주석 처리합니다.

    // Helper: 응답 데이터의 이상 유무를 판단 (Service 내부에 추가)
    private String analyzeResponseData(String responseData) {
        // 엣지 응답은 {"status":"succeed","data":[...]} 형태일 것이므로, 이를 분석합니다.
        try {
            JsonObject json = JsonParser.parseString(responseData).getAsJsonObject();
            if ("succeed".equalsIgnoreCase(json.get("status").getAsString())) {
                return "OK";
            }
        } catch (Exception e) {
            // JSON 파싱 실패 등
        }
        return "ERROR_DATA";
    }

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


    //----1021--- 시작
    @Override
    public Admin_DeviceStatusRespDTO getDeviceStatus(String serialNumber) {
        // 1. DB에서 Device 정보를 조회합니다.
        Device device = adminDeviceRepository.findById(serialNumber)
                .orElseThrow(() -> new RuntimeException("Device not found: " + serialNumber));

        // 2. DTO로 변환하여 장치명과 시리얼넘버를 반환합니다.
        return Admin_DeviceStatusRespDTO.builder()
                .deviceName(device.getName())
                .serialNumber(device.getDeviceSerialNumber())
                .build();
    }

    @Override
    public String testDeviceConnection(String serialNumber) { // String 반환 (패턴 준수)
        long startTime = System.currentTimeMillis();
        String testStatus = "실패";
        String dataStatus = "N/A";
        String responseData = "연결/테스트 실패";
        Device device = null;

        try {
            // 1. 장치 정보 조회 (Device Name을 DTO에 담기 위해 필요)
            device = adminDeviceRepository.findById(serialNumber)
                    .orElseThrow(() -> new RuntimeException("Device not found: " + serialNumber));

            // 2. ConnectionRegistry를 통해 엣지에 실제 테스트 요청 및 응답 수신 (5초 대기)
            // (ConnectionRegistry의 requestTestAndGetResponse 메서드가 동기화 로직을 수행한다고 가정)
            responseData = connectionRegistry.requestTestAndGetResponse(serialNumber);

            // 3. 통신 성공 및 응답 데이터 분석
            testStatus = "성공";
            dataStatus = analyzeResponseData(responseData);

        } catch (Exception e) {
            // 통신 실패 (Connection not found, Timeout, Data Error 등)
            testStatus = "실패";
            responseData = "테스트 오류: " + e.getMessage();
        }

        long endTime = System.currentTimeMillis();

        // DTO 생성 (성공이든 실패든 상세 정보를 모두 담음)
        Admin_DeviceStatusTestDTO resultDTO = Admin_DeviceStatusTestDTO.builder()
                .deviceSerialNumber(serialNumber)
                .name(device != null ? device.getName() : "Unknown Device")
                .status(testStatus)
                .dataStatus(dataStatus)
                .responseData(responseData)
                .responseTimeMs(endTime - startTime)
                .build();

        // 4. 상세 결과를 캐시에 저장합니다.
        testResultCache.put(serialNumber, resultDTO);

        // 5. Controller의 규칙에 맞춰 성공/실패 문자열만 반환합니다.
        return testStatus.equals("성공") ? "success" : "fail";
    }

    @Override
    public Admin_DeviceStatusTestDTO getLatestTestResult(String serialNumber) {
        // Controller가 테스트 성공 후 상세 DTO를 조회하기 위해 호출합니다.
        Admin_DeviceStatusTestDTO result = testResultCache.get(serialNumber);
        if (result == null) {
            // 캐시에 결과가 없으면, 테스트가 실행되지 않았거나 오류 발생으로 간주
            throw new RuntimeException("최근 테스트 결과가 존재하지 않습니다. 먼저 테스트를 실행하세요.");
        }
        return result;
    }
//----1021--- 끝

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
