package shop.mit301.rocket.repository;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import shop.mit301.rocket.domain.DeviceData;
import shop.mit301.rocket.domain.MeasurementData;
import shop.mit301.rocket.domain.MeasurementDataId;

import java.util.List;

public interface Admin_MeasurementDataRepository extends JpaRepository<MeasurementData, MeasurementDataId> {
//    @Transactional
//    void deleteByDevicedata(DeviceData devicedata);
    @Modifying
    @Transactional
    @Query("delete from MeasurementData m where m.devicedata.device.deviceSerialNumber = :serialNumber")
    void deleteByDeviceSerialNumber(@Param("serialNumber") String serialNumber);

}
