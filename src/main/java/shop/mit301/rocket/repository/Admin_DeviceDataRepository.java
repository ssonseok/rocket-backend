package shop.mit301.rocket.repository;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import shop.mit301.rocket.domain.DeviceData;

import java.util.List;

public interface Admin_DeviceDataRepository extends JpaRepository<DeviceData, Integer> {
    // 특정 장치의 DeviceData 조회
    List<DeviceData> findByDevice_DeviceSerialNumber(String deviceSerialNumber);
    //장치에 연결된 데이터들 삭제(자식데이터들 먼저삭제)
    void deleteByDevice_DeviceSerialNumber(String deviceSerialNumber);
    @Modifying
    @Transactional
    @Query("delete from DeviceData d where d.device.deviceSerialNumber = :serialNumber")
    void deleteByDeviceSerialNumber(@Param("serialNumber") String serialNumber);
}
