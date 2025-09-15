package shop.mit301.rocket.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {
    @Id
    @Column(nullable = false, length = 255)
    private String user_id;

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
}
