package shop.mit301.rocket.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "devicedata")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeviceData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "devicedata_id", nullable = false)
    private int deviceData_ID;

    @Column(nullable = false)
    private double min;

    @Column(nullable = false)
    private double max;

    @Column(name = "referencevalue", nullable = false)
    private double referenceValue;

    @Column(nullable = false, length = 255)
    private String name;

    @ManyToOne
    @JoinColumn(name = "unit_id", nullable = false)
    private Unit unit;

    @ManyToOne
    @JoinColumn(name = "device_serialnumber", nullable = false)
    private Device device;

    @OneToMany(mappedBy = "devicedata")
    private List<MeasurementData> measurementDataList = new ArrayList<>();

    @OneToMany(mappedBy = "deviceData")
    private List<User_has_DeviceData> userDeviceDataList = new ArrayList<>();
}
