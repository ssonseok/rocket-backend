package shop.mit301.rocket.domain;

import jakarta.persistence.*;
import lombok.*;

import java.util.Objects;

@Embeddable
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User_has_DeviceDataId {

    @Column(name = "user_id")
    private String user_id;

    @Column(name = "device_data_id")
    private Integer device_data_id;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User_has_DeviceDataId)) return false;
        User_has_DeviceDataId that = (User_has_DeviceDataId) o;
        return Objects.equals(user_id, that.user_id) &&
                Objects.equals(device_data_id, that.device_data_id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(user_id, device_data_id);
    }

}
