package shop.mit301.rocket.service;

import shop.mit301.rocket.domain.Device;
import shop.mit301.rocket.domain.EdgeGateway;
import shop.mit301.rocket.dto.*;

import java.util.List;

public interface Admin_DeviceService {

    // 1. 장비 관리 및 중복 체크 ----------------------------------------------------------------------

    // 시리얼넘버 중복 체크 (유지)
    boolean checkDuplicateSerialNumber(String deviceSerialNumber);

    // 💡 [추가] Edge Gateway 내 포트 경로 중복 체크
    boolean checkDuplicatePortPath(String edgeSerial, String portPath);

    // 장치 등록 (DTO 필드 변경 반영)
    Admin_DeviceRegisterRespDTO registerDevice(Admin_DeviceRegisterReqDTO request);

    // 장치 수정 (DTO 필드 변경 반영)
    String modifyDevice(Admin_DeviceModifyReqDTO dto);

    // 장비 삭제 (유지)
    String deleteDevice(Admin_DeviceDeleteDTO dto);

    // 2. 장비 조회 --------------------------------------------------------------------------------

    // 장비 엔티티 조회 (유지)
    Device getDevice(String serialNumber);

    // 장비 목록 (반환 DTO 필드 변경 반영)
    List<Admin_DeviceListDTO> getDeviceList();

    // 장비 상세 조회 (반환 DTO 필드 변경 반영)
    Admin_DeviceDetailDTO getDeviceDetail(String deviceSerialNumber);

    // 장비 상태 보기 (유지)
    Admin_DeviceStatusRespDTO getDeviceStatus(String serialNumber);

    // 3. 통신 및 테스트 -----------------------------------------------------------------------------

    // 💡 [제거] 기존 testDeviceConnection(String ip, int port) 제거

    // 장치 연결 테스트 (Edge 통해 실제 연결 확인, serialNumber 기반으로 변경)
    String testDeviceConnection(String serialNumber);

    // Controller가 테스트 성공 후 상세 결과를 조회할 메서드 (유지)
    Admin_DeviceStatusTestDTO getLatestTestResult(String serialNumber);


    // 4. 💡 [추가] Edge Gateway 마스터 관리 (EdgeGateway 엔티티를 관리하기 위한 필수 기능)

    // 엣지 게이트웨이 등록
    EdgeGateway registerEdge(EdgeRegisterReqDTO request);

    // 엣지 게이트웨이 조회
    EdgeGateway getEdge(String edgeSerial);

    // 엣지 게이트웨이 목록 조회
    List<EdgeListDTO> getEdgeList();
}
