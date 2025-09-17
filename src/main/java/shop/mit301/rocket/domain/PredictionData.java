package shop.mit301.rocket.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "prediction_data")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PredictionData {

    @EmbeddedId
    private PredictionDataId id;

    @Column(nullable = false)
    private double predicted_value;

    @MapsId("device_data_id")
    @ManyToOne
    @JoinColumn(name = "device_data_id", nullable = false)
    private DeviceData device_data;
}