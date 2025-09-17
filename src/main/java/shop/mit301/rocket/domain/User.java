package shop.mit301.rocket.domain;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class User {
    @Id
    @Column(nullable = false, length = 255, name = "user_id")
    private String userid;

    @Column(nullable = false, length = 255)
    private String pw;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(nullable = false, length = 255)
    private String email;

    @Column(nullable = false, length = 255)
    private String tel;

    @Column(nullable = false, columnDefinition = "TINYINT")
    private byte permission;

    @OneToMany(mappedBy = "user")
    private List<User_has_DeviceData> user_device_data_list = new ArrayList<>();

    @OneToOne
    private PasswordResetToken passwordResetToken;
}
