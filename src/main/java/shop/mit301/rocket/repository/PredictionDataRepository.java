package shop.mit301.rocket.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import shop.mit301.rocket.domain.PredictionData;
import shop.mit301.rocket.domain.PredictionDataId;

import java.time.LocalDateTime;
import java.util.List;

public interface PredictionDataRepository extends JpaRepository<PredictionData, PredictionDataId> {

    @Query("SELECT p FROM PredictionData p " +
            "JOIN FETCH p.device_data d " +
            "JOIN FETCH d.unit u " +
            "WHERE p.id.prediction_date BETWEEN :start AND :end " +
            "AND d.unit.id IN :unitIds " +
            "AND d.device_data_id IN :sensorIds")
    List<PredictionData> findAllByFilter(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end,
            @Param("unitIds") List<Integer> unitIds,
            @Param("sensorIds") List<Integer> sensorIds
    );
}