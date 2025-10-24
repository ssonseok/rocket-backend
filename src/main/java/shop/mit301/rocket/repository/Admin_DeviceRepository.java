package shop.mit301.rocket.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import shop.mit301.rocket.domain.Device;

public interface Admin_DeviceRepository extends JpaRepository<Device, String> {
    // 시리얼넘버 중복 여부 확인 (기존 필드를 사용하므로 유지)
    boolean existsByDeviceSerialNumber(String deviceSerialNumber);

    // [추가] 엣지 게이트웨이 시리얼과 포트 경로 중복 확인 (장비 재등록 방지)
    // Device 엔티티에 새로 추가된 필드를 사용합니다.
    boolean existsByEdgeGateway_EdgeSerialAndPortPath(String edgeSerial, String portPath);
}
