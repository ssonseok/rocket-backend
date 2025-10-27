package shop.mit301.rocket.service;

import shop.mit301.rocket.domain.Device;
import shop.mit301.rocket.dto.*;

import java.util.List;

public interface Admin_DeviceService {

    // 1. 장비 CRUD 관리

    /** 장비 시리얼 중복 체크 */
    boolean checkDuplicateSerialNumber(String deviceSerialNumber);

    /** * 장비의 모든 정보(장치명, Edge IP/Port, 데이터 메타정보)를 단일 메서드로 수정
     * (Admin_DeviceModifyReqDTO는 모든 수정 정보를 담고 있음)
     */
    String updateFullDeviceInfo(
            Admin_DeviceModifyReqDTO request
    );

    /** 장비 삭제 */
    String deleteDevice(Admin_DeviceDeleteDTO dto);


    // 2. 장비 조회 (Read)

    /** 장비 목록 조회 (Admin UI 목록 화면용) */
    List<Admin_DeviceListDTO> getDeviceList();

    /** 장비 상세 정보 조회 (수정 화면 로딩용) */
    Admin_DeviceDetailRespDTO getDeviceDetail(String deviceSerialNumber);

    /** 장비 상태 보기 (응답 데이터 이상 유무, 응답 데이터, 응답 속도 조회) */
    Admin_DeviceStatusTestDTO getDeviceStatus(String serialNumber);
    // Admin_DeviceStatusRespDTO는 아직 정의하지 않았지만, 상태 정보를 담을 응답 DTO가 필요함
}
