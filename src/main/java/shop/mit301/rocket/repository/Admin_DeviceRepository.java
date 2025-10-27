package shop.mit301.rocket.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import shop.mit301.rocket.domain.Device;

import java.util.Optional;

public interface Admin_DeviceRepository extends JpaRepository<Device, String> {

    // 1. 장비 시리얼 중복 확인 (IDeivceRegistrationService에서 사용)
    boolean existsByDeviceSerialNumber(String deviceSerialNumber);

    // 2. 장비 시리얼로 Device 엔티티 조회 (IAdminDeviceService에서 필수)
    Optional<Device> findByDeviceSerialNumber(String deviceSerialNumber);

    // 3.  Edge Gateway의 IP와 Port를 통한 중복 등록 확인
    // EdgeGateway 엔티티 내의 ipAddress와 port 필드를 참조하여 쿼리를 생성합니다.
    boolean existsByEdgeGateway_IpAddressAndEdgeGateway_Port(String ipAddress, int port);
}
