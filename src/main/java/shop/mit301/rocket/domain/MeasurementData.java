package shop.mit301.rocket.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "measurement_data")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MeasurementData {

    @EmbeddedId
    private MeasurementDataId id;

    @Column(nullable = false, name = "measurement_value")
    private double measurementvalue;

    @MapsId("devicedataid")
    @ManyToOne
    @JoinColumn(name = "device_data_id", nullable = false)
    private DeviceData devicedata;
}
