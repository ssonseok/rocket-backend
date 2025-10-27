package shop.mit301.rocket.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import shop.mit301.rocket.domain.Device;
import shop.mit301.rocket.domain.DeviceData;
import shop.mit301.rocket.domain.EdgeGateway;
import shop.mit301.rocket.domain.Unit;
import shop.mit301.rocket.dto.Admin_DeviceConfigFinalizeReqDTO;
import shop.mit301.rocket.dto.Admin_DeviceDataConfigReqDTO;
import shop.mit301.rocket.dto.Admin_DeviceVerificationReqDTO;
import shop.mit301.rocket.dto.Admin_DeviceVerificationRespDTO;
import shop.mit301.rocket.repository.Admin_DeviceDataRepository;
import shop.mit301.rocket.repository.Admin_DeviceRepository;
import shop.mit301.rocket.repository.Admin_UnitRepository;
import shop.mit301.rocket.websocket.EdgeWebSocketHandler;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

@Service
@RequiredArgsConstructor
public class Admin_DeviceRegistrationServiceImpl implements Admin_DeviceRegistrationService {

    private final EdgeGatewayService edgeGatewayService; // Edge IP/Port 관리 서비스
    private final EdgeWebSocketHandler edgeWebSocketHandler; // 실제 통신 담당
    private final Admin_DeviceRepository deviceRepository;
    private final Admin_DeviceDataRepository deviceDataRepository;
    private final Admin_UnitRepository unitRepository; // 단위(Unit) 엔티티 조회용

    //엣지 연결 정보를 검증하고 데이터 개수 수집
    @Override
    @Transactional // EdgeGateway 정보를 갱신하므로 트랜잭션 필요
    public Admin_DeviceVerificationRespDTO verifyConnectionAndGetStreamCount(Admin_DeviceVerificationReqDTO request) {

        String ipAddress = request.getEdgeIp();
        int port = request.getEdgePort();
        String targetSerial = request.getDeviceSerial();

        // 1. IP와 Port를 이용해 DB에서 Edge Serial을 찾아봅니다.
        //    (Edge DB에 이미 등록된 Edge인 경우 Edge Serial이 반환됨)
        Optional<String> edgeSerialOptional = edgeGatewayService.findSerialByConnectionInfo(ipAddress, port);
        String edgeSerial = edgeSerialOptional.orElseThrow(
                () -> new RuntimeException("해당 IP/Port에 연결된 Edge Gateway를 찾을 수 없습니다. 먼저 Edge를 등록하세요.")
        );

        // 2. EdgeGateway 정보를 최신 IP/Port로 갱신하거나 새로 생성합니다.
        //    (상태는 DISCONNECTED 유지)
        edgeGatewayService.findOrCreateEdge(edgeSerial, ipAddress, port);

        // 3. EdgeWebSocketHandler를 통해 Edge 장비에 연결 및 검증 요청을 보냅니다.
        try {
            // EdgeWebSocketHandler에 Edge Serial과 Device Serial을 전달하여 통신 시작
            int dataStreamCount = edgeWebSocketHandler.verifyDeviceConnection(edgeSerial, targetSerial);

            // 4. 응답 DTO 반환
            return Admin_DeviceVerificationRespDTO.builder()
                    .deviceSerial(targetSerial)
                    .dataStreamCount(dataStreamCount)
                    .build();

        } catch (InterruptedException | ExecutionException e) {
            // WebSocket 통신 오류 또는 응답 대기 중 문제 발생
            Thread.currentThread().interrupt();
            throw new RuntimeException("Edge 통신 검증 실패: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new RuntimeException("Edge 통신 검증 실패 (통신/실행 오류): " + e.getMessage(), e);
        }
    }

    //장비와 데이터 정보를 db에 저장
    @Override
    @Transactional
    public Device registerDeviceAndDataStreams(Admin_DeviceConfigFinalizeReqDTO request, String edgeSerial) {

        // 1. EdgeGateway 엔티티 조회 (Device 연결용)
        EdgeGateway edgeGateway = edgeGatewayService.findOrCreateEdge(
                edgeSerial,
                "Unknown IP",
                0 // 이 시점에서는 Edge IP/Port를 DTO에서 받지 않으므로 임시 값 사용 또는 별도 조회
        );

        // 2. Device 엔티티 생성 및 저장
        Device device = Device.builder()
                .deviceSerialNumber(request.getDeviceSerial())
                // Step 1 요청에서 받은 name을 다시 찾아와야 함 (현재 Step 2 DTO에는 name이 없음)
                // -> 설계상 문제가 발생할 수 있으므로, name을 Step 2 DTO에 포함시키거나 DB에서 찾아야 함
                .name("Default Device Name") // 임시 이름 사용
                .edgeGateway(edgeGateway)
                .regist_date(LocalDateTime.now())
                .build();

        Device savedDevice = deviceRepository.save(device);

        // 3. DeviceData 엔티티 목록 생성 및 저장
        for (Admin_DeviceDataConfigReqDTO dataReq : request.getDataStreams()) {

            // 3-1. Unit 엔티티 조회 (UnitName으로 PK 찾기)
            Unit unit = unitRepository.findByUnit(dataReq.getUnitName())
                    .orElseThrow(() -> new RuntimeException("단위(Unit)를 찾을 수 없습니다: " + dataReq.getUnitName()));

            // 3-2. DeviceData 엔티티 생성
            DeviceData deviceData = DeviceData.builder()
                    .device(savedDevice) // 방금 저장한 Device 엔티티 연결
                    .dataIndex(dataReq.getStreamIndex()-1)
                    .name(dataReq.getName())
                    .unit(unit)
                    .min(dataReq.getMinValue())
                    .max(dataReq.getMaxValue())
                    .reference_value(dataReq.getStandardValue())
                    // 🚨🚨 [수정 1] DeviceData: 설정 완료 플래그를 TRUE로 설정 🚨🚨
                    .isConfigured(true)
                    .build();

            deviceDataRepository.save(deviceData);
        }
        savedDevice.completeDataConfiguration();

        // 4. 저장된 Device 엔티티 반환
        return savedDevice;
    }
}
