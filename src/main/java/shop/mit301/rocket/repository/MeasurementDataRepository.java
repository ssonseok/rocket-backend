package shop.mit301.rocket.repository;

import shop.mit301.rocket.domain.DeviceData;
import shop.mit301.rocket.domain.MeasurementData;
import shop.mit301.rocket.domain.MeasurementDataId;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface MeasurementDataRepository extends JpaRepository<MeasurementData, MeasurementDataId> {

    @Query(value = """
        SELECT
            DATE(m.measurement_date) AS period,
            u.unit_id AS unitId,
            d.device_data_id AS sensorId,
            AVG(m.measurement_value) AS avgValue,
            COALESCE(MAX(d.reference_value), 0) AS referenceValue
        FROM measurement_data m
        JOIN device_data d ON m.device_data_id = d.device_data_id
        JOIN unit u ON d.unit_id = u.unit_id
        WHERE m.measurement_date BETWEEN :start AND :end
          AND u.unit_id IN :unitIds
          AND d.device_data_id IN :sensorIds
        GROUP BY period, u.unit_id, d.device_data_id
        ORDER BY period
        """, nativeQuery = true)
    List<Object[]> findAggregatedDaily(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end,
            @Param("unitIds") List<Integer> unitIds,
            @Param("sensorIds") List<Integer> sensorIds
    );

    @Query(value = """
        SELECT
            DATE_SUB(m.measurement_date, INTERVAL (DAYOFWEEK(m.measurement_date) + 5) % 7 DAY) AS period,
            u.unit_id AS unitId,
            d.device_data_id AS sensorId,
            AVG(m.measurement_value) AS avgValue,
            COALESCE(MAX(d.reference_value), 0) AS referenceValue
        FROM measurement_data m
        JOIN device_data d ON m.device_data_id = d.device_data_id
        JOIN unit u ON d.unit_id = u.unit_id
        WHERE m.measurement_date BETWEEN :start AND :end
          AND u.unit_id IN :unitIds
          AND d.device_data_id IN :sensorIds
        GROUP BY period, u.unit_id, d.device_data_id
        ORDER BY period
        """, nativeQuery = true)
    List<Object[]> findAggregatedWeekly(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end,
            @Param("unitIds") List<Integer> unitIds,
            @Param("sensorIds") List<Integer> sensorIds
    );

    @Query(value = """
        SELECT
            DATE_FORMAT(m.measurement_date, '%Y-%m-01') AS period,
            u.unit_id AS unitId,
            d.device_data_id AS sensorId,
            AVG(m.measurement_value) AS avgValue,
            COALESCE(MAX(d.reference_value), 0) AS referenceValue
        FROM measurement_data m
        JOIN device_data d ON m.device_data_id = d.device_data_id
        JOIN unit u ON d.unit_id = u.unit_id
        WHERE m.measurement_date BETWEEN :start AND :end
          AND u.unit_id IN :unitIds
          AND d.device_data_id IN :sensorIds
        GROUP BY period, u.unit_id, d.device_data_id
        ORDER BY period
        """, nativeQuery = true)
    List<Object[]> findAggregatedMonthly(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end,
            @Param("unitIds") List<Integer> unitIds,
            @Param("sensorIds") List<Integer> sensorIds
    );

    @Query(value = """
        SELECT
            DATE_FORMAT(m.measurement_date, '%Y-01-01') AS period,
            u.unit_id AS unitId,
            d.device_data_id AS sensorId,
            AVG(m.measurement_value) AS avgValue,
            COALESCE(MAX(d.reference_value), 0) AS referenceValue
        FROM measurement_data m
        JOIN device_data d ON m.device_data_id = d.device_data_id
        JOIN unit u ON d.unit_id = u.unit_id
        WHERE m.measurement_date BETWEEN :start AND :end
          AND u.unit_id IN :unitIds
          AND d.device_data_id IN :sensorIds
        GROUP BY period, u.unit_id, d.device_data_id
        ORDER BY period
        """, nativeQuery = true)
    List<Object[]> findAggregatedYearly(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end,
            @Param("unitIds") List<Integer> unitIds,
            @Param("sensorIds") List<Integer> sensorIds
    );

    // 기타 기존 메서드들
    Optional<MeasurementData> findTopByDevicedataOrderByIdMeasurementdateDesc(DeviceData devicedata);

    Optional<MeasurementData> findTopByDevicedata_Device_DeviceSerialNumberAndDevicedata_DevicedataidOrderById_MeasurementdateDesc(String deviceSerialNumber, Integer deviceDataId);
}