package shop.mit301.rocket.service;

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
    public List<Admin_DeviceDataDTO> getDeviceDataList(String deviceSerialNumber) {
        List<DeviceData> dataList = deviceDataRepository.findByDevice_DeviceSerialNumber(deviceSerialNumber);

        return dataList.stream().map(d -> {
            Admin_DeviceDataDTO dto = new Admin_DeviceDataDTO();
            dto.setName(d.getName());
            dto.setUnit(d.getUnit().getUnit()); // 문자열 단위
            return dto;
        }).collect(Collectors.toList());
    }

    @Override
    public List<Admin_DeviceDataRegisterRespDTO> registerDeviceData(
            String deviceSerialNumber,
            List<Admin_DeviceDataRegisterReqDTO> requestList) {

        Device device = deviceRepository.findById(deviceSerialNumber)
                .orElseThrow(() -> new RuntimeException("Device not found"));

        List<Admin_DeviceDataRegisterRespDTO> result = new ArrayList<>();

        for (Admin_DeviceDataRegisterReqDTO req : requestList) {

            Unit unit = unitRepository.findById(req.getUnitId())
                    .orElseThrow(() -> new RuntimeException("Unit not found"));

            DeviceData data = DeviceData.builder()
                    .device(device)
                    .name(req.getName())
                    .min(req.getMin())
                    .max(req.getMax())
                    .reference_value(req.getReferenceValue())
                    .unit(unit)
                    .build();

            DeviceData savedData = deviceDataRepository.save(data);

            Admin_DeviceDataRegisterRespDTO resp = new Admin_DeviceDataRegisterRespDTO();
            resp.setName(savedData.getName());
            resp.setMin(savedData.getMin());
            resp.setMax(savedData.getMax());
            resp.setReferenceValue(savedData.getReference_value());
            resp.setUnitId(unit.getUnit_id());
            resp.setSaved(true);

            result.add(resp);
        }

        return result;
    }
}
