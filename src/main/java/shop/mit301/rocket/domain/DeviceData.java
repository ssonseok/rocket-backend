package shop.mit301.rocket.domain;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "device_data")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeviceData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false, name = "device_data_id")
    private int devicedataid;

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

    @OneToMany(mappedBy = "devicedata", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MeasurementData> measurement_data_list = new ArrayList<>();

    @OneToMany(mappedBy = "devicedata", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<User_has_DeviceData> user_device_data_list = new ArrayList<>();

    public void updateDataConfig(double min, double max, double referenceValue, String name, Unit unit) {
        this.min = min;
        this.max = max;
        this.reference_value = referenceValue;
        this.name = name;
        this.unit = unit;
    }
}
