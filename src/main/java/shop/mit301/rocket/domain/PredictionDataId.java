package shop.mit301.rocket.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

@Embeddable
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PredictionDataId implements Serializable {

    @Column(nullable = false, name = "prediction_date")
    private LocalDateTime predictiondate;

    @Column(nullable = false)
    private Integer device_data_id;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PredictionDataId)) return false;
        PredictionDataId that = (PredictionDataId) o;
        return Objects.equals(predictiondate, that.predictiondate) &&
                Objects.equals(device_data_id, that.device_data_id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(predictiondate, device_data_id);
    }
}