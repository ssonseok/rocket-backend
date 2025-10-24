package shop.mit301.rocket.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import shop.mit301.rocket.domain.Device;
import shop.mit301.rocket.domain.DeviceData;
import shop.mit301.rocket.domain.Unit;
import shop.mit301.rocket.dto.Admin_DeviceDataDTO;
import shop.mit301.rocket.dto.Admin_DeviceDataRegisterReqDTO;
import shop.mit301.rocket.dto.Admin_DeviceDataRegisterRespDTO;
import shop.mit301.rocket.repository.Admin_DeviceDataRepository;
import shop.mit301.rocket.repository.Admin_DeviceRepository;
import shop.mit301.rocket.repository.Admin_UnitRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class Admin_DeviceDataServiceImpl implements Admin_DeviceDataService{

    private final Admin_DeviceRepository deviceRepository;
    private final Admin_DeviceDataRepository deviceDataRepository;
    private final Admin_UnitRepository unitRepository;



    @Override
    public List<DeviceData> getDeviceDataList(String deviceSerialNumber) {
        return deviceDataRepository.findByDevice_DeviceSerialNumber(deviceSerialNumber);
    }

    @Override
    @Transactional
    public List<Admin_DeviceDataRegisterRespDTO> registerDeviceData(
            String serialNumber,
            List<Admin_DeviceDataRegisterReqDTO> requestList) {

        // 1. Device 조회
        Device device = deviceRepository.findById(serialNumber)
                .orElseThrow(() -> new RuntimeException("Device not found"));

        // 2. 기존 DeviceData 조회 (엣지 연결 시 자동 생성된 임시 데이터)
        List<DeviceData> existingDataList = deviceDataRepository.findByDevice_DeviceSerialNumber(serialNumber);

        if (existingDataList.size() != requestList.size()) {
            throw new RuntimeException("센서 개수 불일치: DB에 있는 개수(" + existingDataList.size() + ")와 요청 개수(" + requestList.size() + ")가 다릅니다.");
        }

        List<Admin_DeviceDataRegisterRespDTO> result = new ArrayList<>();

        // 3. 기존 DeviceData 업데이트 (새로 INSERT 하지 않음)
        for (int i = 0; i < requestList.size(); i++) {
            Admin_DeviceDataRegisterReqDTO dto = requestList.get(i);
            DeviceData dd = existingDataList.get(i); // 임시 데이터 객체

            Unit unit = unitRepository.findById(dto.getUnitId())
                    .orElseThrow(() -> new RuntimeException("Unit not found"));

            // ✨ 엔티티의 전용 업데이트 메서드 호출
            dd.updateDataConfig(
                    dto.getMin(),
                    dto.getMax(),
                    dto.getReferenceValue(),
                    dto.getName(),
                    unit
            );

            result.add(Admin_DeviceDataRegisterRespDTO.builder()
                    .name(dd.getName())
                    .min(dd.getMin())
                    .max(dd.getMax())
                    .referenceValue(dd.getReference_value())
                    .unitId(unit.getUnitid())
                    .saved(true)
                    .build());
        }

        // 4. 최종 등록 완료 플래그 설정 (✨ 중요: 실시간 측정 시작 활성화)
        device.completeDataConfiguration();
        // @Transactional에 의해 자동으로 DB에 UPDATE 쿼리가 발생합니다.

        return result;
    }

}
