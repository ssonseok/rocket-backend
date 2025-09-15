package shop.mit301.rocket.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "user")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
public class User {
    @Id
    @Column(nullable = false, length = 255)
    private String id;
    @Column(nullable = false, length = 255)
    private String pw;
    @Column(nullable = false, length = 255)
    private String name;
    @Column(nullable = false, length = 255)
    private String email;
    @Column(nullable = false, length = 255)
    private String tel;
    @Column(nullable = false)
    private boolean permission;

//    @OneToMany(mappedBy = "user")
//    private List<User_has_DeviceData> userDeviceDataList = new ArrayList<>();


}
