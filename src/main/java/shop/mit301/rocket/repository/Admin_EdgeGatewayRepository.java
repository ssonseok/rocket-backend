package shop.mit301.rocket.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import shop.mit301.rocket.domain.EdgeGateway;

public interface Admin_EdgeGatewayRepository extends JpaRepository<EdgeGateway, String> {

    // Edge Serial로 엔티티를 조회 (등록 및 테스트 로직에 사용)
    // findById()로도 가능하지만, 명시적으로 findByEdgeSerial()을 추가할 수도 있습니다.

    // IP 주소로 엣지 게이트웨이를 조회 (관리 용이성을 위해)
    EdgeGateway findByIpAddress(String ipAddress);
}
