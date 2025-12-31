package shop.mit301.rocket.repository;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import shop.mit301.rocket.domain.DeviceData;
import shop.mit301.rocket.domain.MeasurementData;
import shop.mit301.rocket.domain.MeasurementDataId;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface Admin_MeasurementDataRepository extends JpaRepository<MeasurementData, MeasurementDataId> {
//    @Transactional
//    void deleteByDevicedata(DeviceData devicedata);
    @Modifying
    @Transactional
    @Query("delete from MeasurementData m where m.devicedata.device.deviceSerialNumber = :serialNumber")
    void deleteByDeviceSerialNumber(@Param("serialNumber") String serialNumber);
    // 이 메서드는 앞서 수정하여 정상입니다.
    @Query("SELECT MAX(m.id.measurementdate) FROM MeasurementData m")
    Optional<LocalDateTime> findLatestMeasurementTime();

    @Query("SELECT m FROM MeasurementData m WHERE m.id.measurementdate = :date")
    List<MeasurementData> findByMeasurementDate(@Param("date") LocalDateTime date);

}
