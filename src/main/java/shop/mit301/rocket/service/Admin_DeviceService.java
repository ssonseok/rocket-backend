package shop.mit301.rocket.service;

import shop.mit301.rocket.dto.*;

import java.util.List;

public interface Admin_DeviceService {
    //시리얼넘버 중복 체크
    boolean checkDuplicateSerialNumber(String deviceSerialNumber);
    // 장치 연결 테스트 (Edge 통해 실제 연결 확인 필요)
    String testDeviceConnection(String ip, int port);
    // 장치 등록
    Admin_DeviceRegisterRespDTO registerDevice(Admin_DeviceRegisterReqDTO request);
    //장비 목록
    List<Admin_DeviceListDTO> getDeviceList();
    //장비 삭제
    String deleteDevice(Admin_DeviceDeleteDTO dto);
    //장비 수정
    String modifyDevice(Admin_DeviceModifyReqDTO dto);
    //장비 상태보기
    Admin_DeviceStatusRespDTO getDeviceStatus(String serialNumber);
    Admin_DeviceStatusTestDTO testDeviceConnection(String serialNumber);
    //수정화면에서 필요한 조회
    Admin_DeviceDetailDTO getDeviceDetail(String deviceSerialNumber);

}
