package shop.mit301.rocket.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.hibernate.annotations.Type;

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

    @OneToMany(mappedBy = "device", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<DeviceData> device_data_list = new ArrayList<>();

    @Column(nullable = false)
    private boolean is_data_configured = false;

    public void completeDataConfiguration() {
        this.is_data_configured = true;
    }

    @Builder
    public Device(String deviceSerialNumber, int port, String ip, String name, LocalDateTime regist_date, LocalDateTime modify_date, List<DeviceData> device_data_list) {
        this.deviceSerialNumber = deviceSerialNumber;
        this.port = port;
        this.ip = ip;
        this.name = name;
        this.regist_date = regist_date;
        this.modify_date = modify_date;
        this.device_data_list = device_data_list;
        this.is_data_configured = false; // 기본값
    }

}
