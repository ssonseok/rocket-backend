package shop.mit301.rocket.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import shop.mit301.rocket.domain.DeviceData;

import java.util.List;

public interface Admin_DeviceDataRepository extends JpaRepository<DeviceData, Integer> {
    // 특정 장치의 DeviceData 조회
    List<DeviceData> findByDevice_DeviceSerialNumber(String deviceSerialNumber);
}
