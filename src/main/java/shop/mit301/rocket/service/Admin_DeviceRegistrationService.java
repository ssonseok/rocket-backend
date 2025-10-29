package shop.mit301.rocket.service;

import shop.mit301.rocket.domain.Device;
import shop.mit301.rocket.dto.Admin_DeviceConfigFinalizeReqDTO;
import shop.mit301.rocket.dto.Admin_DeviceVerificationReqDTO;
import shop.mit301.rocket.dto.Admin_DeviceVerificationRespDTO;

public interface Admin_DeviceRegistrationService {
    /** * Step 1: UI 요청을 받아 Edge IP/Port 유효성 검증 및 데이터 스트림 개수 요청
     * @return Edge 통신 성공 시 반환되는 데이터 스트림 개수 및 Serial을 포함하는 응답 DTO
     */
    Admin_DeviceVerificationRespDTO verifyConnectionAndGetStreamCount(
            Admin_DeviceVerificationReqDTO request
    );

    /** * Step 2: 데이터 스트림 설정 후 장비(Device) 및 데이터(DeviceData)를 최종 등록
     * @param edgeSerial Step 1에서 찾은 Edge Serial (ServiceImpl 내부에서 사용)
     */
    Device registerDeviceAndDataStreams(
            Admin_DeviceConfigFinalizeReqDTO request,
            String edgeSerial
    );

}
