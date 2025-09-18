package shop.mit301.rocket.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import shop.mit301.rocket.domain.DeviceData;

public interface Admin_DeviceDataRepository extends JpaRepository<DeviceData, Integer> {

}
