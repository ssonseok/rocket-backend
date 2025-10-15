package shop.mit301.rocket.websocket;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ConnectionRegistry {

    // deviceSerialNumber -> WebSocketSession
    private final Map<String, WebSocketSession> connections = new ConcurrentHashMap<>();

    public void register(String deviceSerial, WebSocketSession session) {
        connections.put(deviceSerial, session);
    }

    public void unregister(WebSocketSession session) {
        connections.values().removeIf(s -> s.getId().equals(session.getId()));
    }

    public boolean isConnected(String deviceSerial) {
        WebSocketSession session = connections.get(deviceSerial);
        return session != null && session.isOpen();
    }

    public WebSocketSession getSession(String deviceSerial) {
        return connections.get(deviceSerial);
    }
}
