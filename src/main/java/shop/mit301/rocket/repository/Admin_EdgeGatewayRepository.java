package shop.mit301.rocket.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import shop.mit301.rocket.domain.EdgeGateway;

import java.util.Optional;

public interface Admin_EdgeGatewayRepository extends JpaRepository<EdgeGateway, String> {
    Optional<EdgeGateway> findByIpAddressAndPort(String ipAddress, Integer port);
}
