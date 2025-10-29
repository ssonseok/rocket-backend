package shop.mit301.rocket.service;

import shop.mit301.rocket.domain.EdgeGateway;

import java.util.Optional;

public interface EdgeGatewayService {
    /** IP/Port로 Edge Serial을 DB에서 찾아 자동으로 결정 */
    Optional<String> findSerialByConnectionInfo(String ipAddress, int port);

    /** Edge Serial 기준으로 찾거나 생성하고, IP/Port 및 상태를 갱신 */
    EdgeGateway findOrCreateEdge(String edgeSerial, String ipAddress, int port);

    /** WebSocket 연결/해제 시 Edge 상태 업데이트 */
    void updateStatus(String edgeSerial, String status);
}
