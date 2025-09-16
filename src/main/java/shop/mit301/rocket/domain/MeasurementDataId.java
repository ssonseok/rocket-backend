package shop.mit301.rocket.domain;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

@Embeddable
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class MeasurementDataId implements Serializable {

    @Column(nullable = false)
    private LocalDateTime measurement_date;

    @Column(nullable = false)
    private Integer device_data_id;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MeasurementDataId)) return false;
        MeasurementDataId that = (MeasurementDataId) o;
        return Objects.equals(measurement_date, that.measurement_date) &&
                Objects.equals(device_data_id, that.device_data_id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(measurement_date, device_data_id);
    }
}
