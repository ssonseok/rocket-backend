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
@Builder
public class EdgeGateway {

    // 엣지 게이트웨이 시리얼 (예: "1001")을 기본 키로 사용
    @Id
    @Column(nullable = false, length = 20, columnDefinition = "VARCHAR(20)", name = "edge_serial")
    private String edgeSerial;

    @Column(nullable = false, length = 15)
    private String ipAddress; // 엣지 장치의 실제 IP 주소(192.168.1.119 or 192.168.1.120 등등)

    @Column(nullable = false, length = 20)
    private String status; // 연결 상태 (CONNECTED, DISCONNECTED 등)

    // 이 게이트웨이에 연결된 장비 목록 (1:N 관계)
    @OneToMany(mappedBy = "edgeGateway", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<Device> deviceList = new ArrayList<>();
}
