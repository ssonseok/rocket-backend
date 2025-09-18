package shop.mit301.rocket.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import shop.mit301.rocket.domain.DeviceData;

import java.util.List;

public interface DeviceDataRepository extends JpaRepository<DeviceData, Integer> {
    List<DeviceData> findByDevicedataidIn(List<Integer> ids);
}