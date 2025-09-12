package shop.mit301.rocket.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "device")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Device {

    @Id
    @Column(name = "device_serialnumber", nullable = false, length = 8, columnDefinition = "CHAR(8)")
    private String device_SerialNumber;

    @Column(nullable = false)
    private int port;

    @Column(nullable = false, length = 15, columnDefinition = "CHAR(15)")
    private String ip;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(name = "registdate", nullable = false)
    private LocalDateTime registDate;

    @Column(name = "modifydate", nullable = true)
    private LocalDateTime modifyDate;

    @OneToMany(mappedBy = "device")
    private List<DeviceData> deviceDataList = new ArrayList<>();
}
