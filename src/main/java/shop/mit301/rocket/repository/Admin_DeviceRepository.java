package shop.mit301.rocket.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import shop.mit301.rocket.domain.Device;

public interface Admin_DeviceRepository extends JpaRepository<Device, String> {
    // 시리얼넘버 중복 여부 확인
    boolean existsByDeviceSerialNumber(String deviceSerialNumber);
}
