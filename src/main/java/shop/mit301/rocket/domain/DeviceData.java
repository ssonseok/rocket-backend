package shop.mit301.rocket.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "device_data")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeviceData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private int device_data_id;

    @Column(nullable = false)
    private double min;

    @Column(nullable = false)
    private double max;

    @Column(nullable = false)
    private double reference_value;

    @Column(nullable = false, length = 255)
    private String name;

    @ManyToOne
    @JoinColumn(name = "unit_id", nullable = false)
    private Unit unit;

    @ManyToOne
    @JoinColumn(name = "device_serial_number", nullable = false)
    private Device device;

    @OneToMany(mappedBy = "device_data")
    private List<MeasurementData> measurement_data_list = new ArrayList<>();

    @OneToMany(mappedBy = "device_data")
    private List<User_has_DeviceData> user_device_data_list = new ArrayList<>();
}
