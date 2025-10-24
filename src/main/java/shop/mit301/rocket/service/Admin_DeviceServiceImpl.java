package shop.mit301.rocket.service;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import shop.mit301.rocket.domain.Device;
import shop.mit301.rocket.domain.DeviceData;
import shop.mit301.rocket.domain.EdgeGateway;
import shop.mit301.rocket.dto.*;
import shop.mit301.rocket.repository.Admin_DeviceDataRepository;
import shop.mit301.rocket.repository.Admin_DeviceRepository;
import shop.mit301.rocket.repository.Admin_EdgeGatewayRepository;
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
    private final Admin_EdgeGatewayRepository adminEdgeGatewayRepository; // 💡 [추가] Edge Gateway Repository
    // private final Admin_UnitRepository adminUnitRepository; // 사용하지 않아 주석 처리
    private final Admin_DeviceDataRepository adminDeviceDataRepository;
    private final ModelMapper modelMapper;
    private final ConnectionRegistry connectionRegistry;

    // 필드 추가: 가장 최근 테스트 결과를 임시 저장할 인메모리 캐시
    private final Map<String, Admin_DeviceStatusTestDTO> testResultCache = new ConcurrentHashMap<>();

    // Helper: 응답 데이터의 이상 유무를 판단
    private String analyzeResponseData(String responseData) {
        try {
            // Gson 대신 JsonParser 사용
            JsonObject json = JsonParser.parseString(responseData).getAsJsonObject();
            String status = json.get("status").getAsString();

            if ("success".equalsIgnoreCase(status) || "succeed".equalsIgnoreCase(status)) {
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

    // 💡 [추가] Edge Gateway 내 포트 경로 중복 확인 (장비 재등록 방지)
    public boolean checkDuplicatePortPath(String edgeSerial, String portPath) {
        return adminDeviceRepository.existsByEdgeGateway_EdgeSerialAndPortPath(edgeSerial, portPath);
    }

    @Override
    @Transactional
    public Admin_DeviceRegisterRespDTO registerDevice(Admin_DeviceRegisterReqDTO request) {
        // 1. 시리얼 넘버 중복 체크
        if (checkDuplicateSerialNumber(request.getDeviceSerialNumber())) {
            // DTO에 ip/port 필드가 없으므로, 제거 후 빌드
            return Admin_DeviceRegisterRespDTO.builder()
                    .deviceSerialNumber(request.getDeviceSerialNumber())
                    .name(request.getName())
                    .testSuccess(false)
                    .dataCount(0)
                    .build();
        }

        // 2. 💡 [필수] Edge Gateway 존재 여부 확인 및 엔티티 조회
        EdgeGateway edgeGateway = adminEdgeGatewayRepository.findById(request.getEdgeSerial())
                .orElseThrow(() -> new RuntimeException("Edge Gateway를 찾을 수 없습니다: " + request.getEdgeSerial()));

        // 3. 💡 [추가] 같은 Edge 내 포트 경로 중복 체크
        if (checkDuplicatePortPath(request.getEdgeSerial(), request.getPortPath())) {
            throw new RuntimeException("해당 Edge Gateway에 이미 같은 포트 경로를 사용하는 장비가 등록되어 있습니다.");
        }


        // 4. 장치 등록 (ip/port 제거, edgeGateway/portPath 추가)
        Device device = Device.builder()
                .deviceSerialNumber(request.getDeviceSerialNumber())
                .name(request.getName())
                .edgeGateway(edgeGateway) // 💡 [수정] EdgeGateway 엔티티 연결
                .portPath(request.getPortPath()) // 💡 [수정] Port Path 저장
                .regist_date(LocalDateTime.now())
                .build();
        adminDeviceRepository.save(device);

        // 등록 시점에는 DeviceData가 없으므로 0으로 설정
        int sensorCount = adminDeviceDataRepository.findByDevice_DeviceSerialNumber(device.getDeviceSerialNumber()).size();

        // 5. 응답 DTO 필드 수정 (ip/port 제거)
        return Admin_DeviceRegisterRespDTO.builder()
                .deviceSerialNumber(device.getDeviceSerialNumber())
                .name(device.getName())
                .testSuccess(true)
                .dataCount(sensorCount)
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

            // 💡 [추가] Edge Serial 및 Port Path 표시
            dto.setEdgeSerial(device.getEdgeGateway().getEdgeSerial());
            dto.setPortPath(device.getPortPath());

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
        // 로직 유지 (DB 관계 설정이 올바르다면 Cascade로 하위 데이터까지 삭제)
        Device device = adminDeviceRepository.findById(dto.getDeviceSerialNumber())
                .orElseThrow(() -> new RuntimeException("Device not found"));

        // DeviceData와 MeasurementData 리스트를 강제로 fetch
        device.getDevice_data_list().forEach(dd -> {
            dd.getMeasurement_data_list().size(); // Lazy 강제 초기화
            dd.getUser_device_data_list().size();
        });

        adminDeviceRepository.delete(device);
        return "success";
    }

    @Override
    @Transactional
    public String modifyDevice(Admin_DeviceModifyReqDTO dto) {
        Device existing = adminDeviceRepository.findById(dto.getDeviceSerialNumber()).get();

        EdgeGateway edgeGateway = adminEdgeGatewayRepository.findById(dto.getEdgeSerial())
                .orElseThrow(() -> new RuntimeException("Edge Gateway를 찾을 수 없습니다: " + dto.getEdgeSerial()));

        Device updated = Device.builder()
                .deviceSerialNumber(existing.getDeviceSerialNumber())
                .name(dto.getName())
                .edgeGateway(edgeGateway)  // 💡 [수정] EdgeGateway 엔티티 연결
                .portPath(dto.getPortPath()) // 💡 [수정] Port Path 저장
                .regist_date(existing.getRegist_date())
                .build();

        adminDeviceRepository.save(updated);

        return "success";
    }


    //----1021--- 시작
    @Override
    public Admin_DeviceStatusRespDTO getDeviceStatus(String serialNumber) {
        // 로직 유지 (Device Name, SerialNumber 기반이므로 변경 없음)
        Device device = adminDeviceRepository.findById(serialNumber)
                .orElseThrow(() -> new RuntimeException("Device not found: " + serialNumber));

        return Admin_DeviceStatusRespDTO.builder()
                .deviceName(device.getName())
                .serialNumber(device.getDeviceSerialNumber())
                .build();
    }

    @Override
    public String testDeviceConnection(String serialNumber) {
        long startTime = System.currentTimeMillis();
        String testStatus = "실패";
        String dataStatus = "N/A";
        String responseData = "연결/테스트 실패";
        Device device = null;
        String edgeSerial = null;
        String portPath = null; // 💡 [추가] portPath 변수 선언

        try {
            // 1. 장치 정보 조회 (EdgeSerial과 PortPath를 얻기 위함)
            device = adminDeviceRepository.findById(serialNumber)
                    .orElseThrow(() -> new RuntimeException("Device not found: " + serialNumber));

            // 2. 💡 [추출] EdgeSerial과 PortPath 추출
            edgeSerial = device.getEdgeGateway().getEdgeSerial();
            portPath = device.getPortPath(); // 💡 [추가] portPath 추출

            // 3. ConnectionRegistry를 통해 엣지에 실제 테스트 요청 및 응답 수신
            // (이제 ConnectionRegistry는 edgeSerial, deviceSerial, portPath 3개를 받습니다.)
            responseData = connectionRegistry.requestTestAndGetResponse(
                    edgeSerial,
                    serialNumber,
                    portPath // 💡 [수정] portPath 전달
            );

            // 4. 통신 성공 및 응답 데이터 분석
            testStatus = "성공";
            dataStatus = analyzeResponseData(responseData);

        } catch (Exception e) {
            testStatus = "실패";
            responseData = "테스트 오류: " + e.getMessage();
        }

        long endTime = System.currentTimeMillis();

        // DTO 생성 (캐시 저장)
        Admin_DeviceStatusTestDTO resultDTO = Admin_DeviceStatusTestDTO.builder()
                .deviceSerialNumber(serialNumber)
                .name(device != null ? device.getName() : "Unknown Device")
                .status(testStatus)
                .dataStatus(dataStatus)
                .responseData(responseData)
                .responseTimeMs(endTime - startTime)
                .edgeSerial(edgeSerial)
                .portPath(portPath) // 💡 [수정] 추출한 portPath 사용
                .build();

        // 5. 상세 결과를 캐시에 저장합니다.
        testResultCache.put(serialNumber, resultDTO);

        return testStatus.equals("성공") ? "success" : "fail";
    }

    @Override
    public Admin_DeviceStatusTestDTO getLatestTestResult(String serialNumber) {
        // 로직 유지
        Admin_DeviceStatusTestDTO result = testResultCache.get(serialNumber);
        if (result == null) {
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

        // 3. 센서 DTO 변환 (유지)
        List<Admin_DeviceDataRegisterRespDTO> sensors = dataList.stream()
                .map(data -> Admin_DeviceDataRegisterRespDTO.builder()
                        .name(data.getName())
                        .min(data.getMin())
                        .max(data.getMax())
                        .referenceValue(data.getReference_value())
                        .unitId(data.getUnit().getUnitid())
                        .saved(true)
                        .build()
                ).collect(Collectors.toList());

        // 4. 장치 DTO 변환 (ip/port 제거)
        return Admin_DeviceDetailDTO.builder()
                .deviceSerialNumber(device.getDeviceSerialNumber())
                .name(device.getName())
                .edgeSerial(device.getEdgeGateway().getEdgeSerial()) // 💡 [수정]
                .portPath(device.getPortPath()) // 💡 [수정]
                .deviceDataList(sensors)
                .build();
    }

    // 💡 [제거] testDeviceConnection(String ip, int port) 메서드는 제거되어야 합니다.
    // @Override
    // public String testDeviceConnection(String ip, int port) { return null; }
    @Override
    @Transactional
    public EdgeGateway registerEdge(EdgeRegisterReqDTO request) {
        // 1. 중복 체크 (Edge Serial은 PK이므로, findById로 존재 여부 확인 가능)
        if (adminEdgeGatewayRepository.existsById(request.getEdgeSerial())) {
            throw new IllegalArgumentException("Edge Gateway 시리얼 넘버가 이미 존재합니다: " + request.getEdgeSerial());
        }

        // 2. EdgeGateway 엔티티 생성
        EdgeGateway edgeGateway = EdgeGateway.builder()
                .edgeSerial(request.getEdgeSerial())
                .ipAddress(request.getIpAddress())
                // 요청 DTO에 status가 없다면 기본값 "DISCONNECTED" 사용
                .status(request.getStatus() != null ? request.getStatus() : "DISCONNECTED")
                .build();

        // 3. 저장 및 반환
        return adminEdgeGatewayRepository.save(edgeGateway);
    }

    @Override
    public EdgeGateway getEdge(String edgeSerial) {
        // 엣지 게이트웨이 조회
        return adminEdgeGatewayRepository.findById(edgeSerial)
                .orElseThrow(() -> new RuntimeException("Edge Gateway를 찾을 수 없습니다: " + edgeSerial));
    }

    @Override
    public List<EdgeListDTO> getEdgeList() {
        // 1. 모든 Edge Gateway 엔티티 조회
        List<EdgeGateway> edgeList = adminEdgeGatewayRepository.findAll();

        // 2. DTO로 변환
        return edgeList.stream().map(edge -> {

            // 💡 Edge Gateway에 연결된 장비 수 계산
            int deviceCount = edge.getDeviceList().size();

            return EdgeListDTO.builder()
                    .edgeSerial(edge.getEdgeSerial())
                    .ipAddress(edge.getIpAddress())
                    .status(edge.getStatus())
                    .deviceCount(deviceCount) // 연결된 장비 수 포함
                    .build();
        }).collect(Collectors.toList());
    }
}
