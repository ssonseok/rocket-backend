package shop.mit301.rocket.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import shop.mit301.rocket.domain.MeasurementData;
import shop.mit301.rocket.domain.MeasurementDataId;

import java.util.List;

public interface Admin_MeasurementDataRepository extends JpaRepository<MeasurementData, MeasurementDataId> {

}
