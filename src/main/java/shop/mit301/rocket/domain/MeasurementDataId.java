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

    @Column(nullable = false, name = "measurement_date")
    private LocalDateTime measurementdate;

    @Column(nullable = false, name = "device_data_id")
    private Integer devicedataid;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MeasurementDataId)) return false;
        MeasurementDataId that = (MeasurementDataId) o;
        return Objects.equals(measurementdate, that.measurementdate) &&
                Objects.equals(devicedataid, that.devicedataid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(measurementdate, devicedataid);
    }
}
