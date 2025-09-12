package shop.mit301.rocket.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "measurementdata")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MeasurementData {

    @EmbeddedId
    private MeasurementDataId id;

    @Column(name = "measurementvalue", nullable = false)
    private double measurementValue;

    @MapsId("deviceDataId")
    @ManyToOne
    @JoinColumn(name = "devicedata_id", nullable = false)
    private DeviceData deviceData;
}
