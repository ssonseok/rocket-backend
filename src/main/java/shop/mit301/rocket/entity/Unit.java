package shop.mit301.rocket.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "unit")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Unit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "unit_id", nullable = false)
    private int unit_ID;

    @Column(nullable = true, length = 255)
    private String unit;

    @OneToMany(mappedBy = "unit")
    private List<DeviceData> deviceDataList = new ArrayList<>();
}
