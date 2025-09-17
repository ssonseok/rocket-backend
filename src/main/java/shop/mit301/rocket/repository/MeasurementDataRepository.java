package shop.mit301.rocket.repository;

import shop.mit301.rocket.domain.MeasurementData;
import shop.mit301.rocket.domain.MeasurementDataId;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface MeasurementDataRepository extends JpaRepository<MeasurementData, MeasurementDataId> {

    @Query("SELECT m FROM MeasurementData m " +
            "JOIN FETCH m.device_data d " +
            "JOIN FETCH d.unit u " +
            "WHERE m.id.measurement_date BETWEEN :start AND :end " +
            "AND d.unit.id IN :unitIds " +
            "AND d.device_data_id IN :sensorIds")
    List<MeasurementData> findAllByFilter(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end,
            @Param("unitIds") List<Integer> unitIds,
            @Param("sensorIds") List<Integer> sensorIds
    );
}