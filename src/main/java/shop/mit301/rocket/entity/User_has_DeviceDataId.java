package shop.mit301.rocket.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Objects;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User_has_DeviceDataId {

    @Column(name = "user_id")
    private String userId;

    @Column(name = "devicedata_id")
    private Integer deviceDataId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User_has_DeviceDataId)) return false;
        User_has_DeviceDataId that = (User_has_DeviceDataId) o;
        return Objects.equals(userId, that.userId) &&
                Objects.equals(deviceDataId, that.deviceDataId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, deviceDataId);
    }

}
