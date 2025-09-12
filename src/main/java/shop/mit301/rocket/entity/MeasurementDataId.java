package shop.mit301.rocket.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MeasurementDataId implements Serializable {

    @Column(name = "measurementdate", nullable = false)
    private LocalDateTime measurementDate;

    @Column(name = "devicedata_id", nullable = false)
    private Integer deviceDataId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MeasurementDataId)) return false;
        MeasurementDataId that = (MeasurementDataId) o;
        return Objects.equals(measurementDate, that.measurementDate) &&
                Objects.equals(deviceDataId, that.deviceDataId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(measurementDate, deviceDataId);
    }
}
