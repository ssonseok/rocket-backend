package shop.mit301.rocket.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "measurement_data")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MeasurementData {

    @EmbeddedId
    private MeasurementDataId id;

    @Column(nullable = false)
    private double measurement_value;

    @MapsId("device_data_id")
    @ManyToOne
    @JoinColumn(name = "device_data_id", nullable = false)
    private DeviceData device_data;
}
