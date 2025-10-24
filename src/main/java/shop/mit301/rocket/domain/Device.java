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
    private String deviceSerialNumber; // 장비 고유 시리얼 (예: device1)

    //  [추가] Edge Gateway 참조 (FK): 이 장비가 어느 엣지에 연결되었는지 명확히 함
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "edge_serial", nullable = false)
    private EdgeGateway edgeGateway;

    //  [추가] Edge Gateway 내부의 물리적 포트 경로 (문자열 타입)
    @Column(nullable = false, length = 30, name = "port_path")
    private String portPath;

    //  [제거] 기존의 int port; 필드와 String ip; 필드가 이 구조에서 제거됨

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

    // @Builder 생성자 수정 (ip와 port 대신 edgeGateway와 portPath 사용)
    @Builder
    public Device(String deviceSerialNumber, EdgeGateway edgeGateway, String portPath, String name, LocalDateTime regist_date, LocalDateTime modify_date, List<DeviceData> device_data_list) {
        this.deviceSerialNumber = deviceSerialNumber;
        this.edgeGateway = edgeGateway;
        this.portPath = portPath;
        this.name = name;
        this.regist_date = regist_date;
        this.modify_date = modify_date;
        this.device_data_list = device_data_list;
        this.is_data_configured = false;
    }
}
