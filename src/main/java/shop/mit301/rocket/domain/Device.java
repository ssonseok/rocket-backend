package shop.mit301.rocket.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "device")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Device {

    @Id
    @Column(nullable = false, length = 8, columnDefinition = "CHAR(8)", name = "device_serial_number")
    private String deviceSerialNumber;

    @Column(nullable = false)
    private int port;

    @Column(nullable = false, length = 15, columnDefinition = "CHAR(15)")
    private String ip;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(nullable = false)
    private LocalDateTime regist_date;

    @Column(nullable = true)
    private LocalDateTime modify_date;

    @OneToMany(mappedBy = "device")
    private List<DeviceData> device_data_list = new ArrayList<>();
}
