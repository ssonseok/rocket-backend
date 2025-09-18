package shop.mit301.rocket.domain;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "unit")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Unit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false, name = "unit_id")
    private int unitid;

    @Column(nullable = true, length = 255)
    private String unit;

    @OneToMany(mappedBy = "unit")
    private List<DeviceData> device_data_list = new ArrayList<>();
}
