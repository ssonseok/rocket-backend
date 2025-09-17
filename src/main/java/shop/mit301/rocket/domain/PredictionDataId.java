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

    @Column(nullable = false)
    private LocalDateTime prediction_date;

    @Column(nullable = false)
    private Integer device_data_id;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PredictionDataId)) return false;
        PredictionDataId that = (PredictionDataId) o;
        return Objects.equals(prediction_date, that.prediction_date) &&
                Objects.equals(device_data_id, that.device_data_id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(prediction_date, device_data_id);
    }
}