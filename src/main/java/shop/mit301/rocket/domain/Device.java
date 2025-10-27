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
@Builder(toBuilder = true)
public class Device {

    @Id
    @Column(nullable = false, length = 255, columnDefinition = "CHAR(255)", name = "device_serial_number")
    private String deviceSerialNumber; // 장비 고유 시리얼 (예: device1)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "edge_serial", nullable = false)
    private EdgeGateway edgeGateway;



    @Column(nullable = false, length = 255)
    private String name;//장치명

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
    public Device(String deviceSerialNumber, EdgeGateway edgeGateway,String name, LocalDateTime regist_date, LocalDateTime modify_date, List<DeviceData> device_data_list) {
        this.deviceSerialNumber = deviceSerialNumber;
        this.edgeGateway = edgeGateway;
        this.name = name;
        this.regist_date = regist_date;
        this.modify_date = modify_date;
        this.device_data_list = device_data_list;
        this.is_data_configured = false;
    }//패스포트 제거 상태
}
