package shop.mit301.rocket.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import shop.mit301.rocket.domain.EdgeGateway;
import shop.mit301.rocket.repository.Admin_EdgeGatewayRepository;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class EdgeGatewayServiceImpl implements EdgeGatewayService {

    // Admin_EdgeGatewayRepository를 사용하여 DB 접근
    private final Admin_EdgeGatewayRepository edgeGatewayRepository;

    /**
     * IP와 Port를 기반으로 DB에서 Edge Serial을 찾아 반환합니다.
     * 장비 등록 Step 1에서 Edge Serial을 자동으로 결정하는 핵심 로직입니다.
     */
    @Override
    public Optional<String> findSerialByConnectionInfo(String ipAddress, int port) {

        // Repository에 정의한 findByIpAddressAndPort 메서드를 사용하여 조회합니다.
        return edgeGatewayRepository.findByIpAddressAndPort(ipAddress, port)
                // 조회된 EdgeGateway 엔티티에서 edgeSerial(PK)만 추출하여 반환합니다.
                .map(EdgeGateway::getEdgeSerial);
    }

    /**
     * EdgeGateway 엔티티를 Edge Serial(PK)을 기준으로 찾거나,
     * 존재하지 않으면 UI에서 받은 정보로 새로 생성하고 DB에 저장합니다.
     */
    @Override
    @Transactional
    public EdgeGateway findOrCreateEdge(String edgeSerial, String ipAddress, int port) {

        return edgeGatewayRepository.findById(edgeSerial)
                .map(edge -> {
                    // 1. 이미 존재하는 경우 (Update): IP와 Port 정보 갱신 및 상태는 그대로 유지
                    // EdgeGateway 엔티티에 updateConnectionInfo 메서드가 있다고 가정
                    edge.updateConnectionInfo(ipAddress, port, edge.getStatus());
                    return edge;
                })
                .orElseGet(() -> {
                    // 2. 존재하지 않는 경우 (Create): 새로운 EdgeGateway 생성 및 저장
                    EdgeGateway newEdge = EdgeGateway.builder()
                            .edgeSerial(edgeSerial) // Controller에서 찾은 Edge Serial 사용
                            .ipAddress(ipAddress)
                            .port(port)
                            .name("New Edge Gateway - " + LocalDateTime.now()) // 기본 이름 설정
                            .status("DISCONNECTED") // 초기 상태는 연결 끊김
                            .build();
                    return edgeGatewayRepository.save(newEdge);
                });
    }

    /**
     * WebSocket 연결/해제 시 EdgeGateway의 상태를 DB에 업데이트합니다.
     */
    @Override
    @Transactional
    public void updateStatus(String edgeSerial, String status) {
        // Edge Serial로 엔티티를 찾은 후 상태를 업데이트합니다.
        edgeGatewayRepository.findById(edgeSerial).ifPresent(edge -> {
            // EdgeGateway 엔티티의 updateConnectionInfo 메서드를 사용하여 상태 필드만 업데이트
            edge.updateConnectionInfo(edge.getIpAddress(), edge.getPort(), status);
        });
    }
}
