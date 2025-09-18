package shop.mit301.rocket.service;

import shop.mit301.rocket.dto.Admin_DeviceDataDTO;
import shop.mit301.rocket.dto.Admin_DeviceDataRegisterReqDTO;
import shop.mit301.rocket.dto.Admin_DeviceDataRegisterRespDTO;

import java.util.List;

public interface Admin_DeviceDataService {
    // 1) 특정 장치의 장치 데이터 목록 조회 (엣지에서 받은 코드/이름/단위)
    List<Admin_DeviceDataDTO> getDeviceDataList(String deviceSerialNumber);

    // 2) 장치 데이터 등록 (UI에서 입력한 min/max/reference/unitId 포함)
    List<Admin_DeviceDataRegisterRespDTO> registerDeviceData(
            String deviceSerialNumber,
            List<Admin_DeviceDataRegisterReqDTO> requestList
    );
}
