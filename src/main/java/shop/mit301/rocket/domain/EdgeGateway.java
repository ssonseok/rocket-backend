package shop.mit301.rocket.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "edge_gateway")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class EdgeGateway {

    // 엣지 시리얼PK deviceN 으로 설정할거임
    @Id
    @Column(nullable = false, length = 20, columnDefinition = "VARCHAR(20)", name = "edge_serial")
    private String edgeSerial;

    @Column(nullable = false, length = 15)
    private String ipAddress; // 엣지 장치의 실제 IP 주소(192.168.1.119 or 192.168.1.120 등등)
    @Column(nullable = true)
    private Integer port; //8081 8082로 설정(백엔드가 8080임)

    @Column(nullable = false, length = 20)
    private String status; // 연결 상태 (CONNECTED, DISCONNECTED)
    //엣지시리얼 이름 edgeSN1 edgeSN2 ....
    @Column(nullable = true, length = 100)
    private String name;

    // 이 게이트웨이에 연결된 장비 목록 (1:N 관계)
    @OneToMany(mappedBy = "edgeGateway", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<Device> deviceList = new ArrayList<>();

    public void updateConnectionInfo(String ipAddress, Integer port, String status) {
        // IP와 Port는 null 체크 없이 바로 업데이트 (UI에서 항상 유효한 값을 받는다고 가정)
        this.ipAddress = ipAddress;
        this.port = port;
        // Status는 null이 아닐 때만 업데이트하여, 기존 status를 덮어쓰지 않도록 방어 로직을 넣는 것이 좋습니다.
        if (status != null) {
            this.status = status;
        }
    }
}
