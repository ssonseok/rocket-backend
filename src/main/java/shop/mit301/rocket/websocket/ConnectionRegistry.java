package shop.mit301.rocket.websocket;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeoutException;

@Component
public class ConnectionRegistry {

    // deviceSerialNumber -> WebSocketSession
    private final Map<String, WebSocketSession> connections = new ConcurrentHashMap<>();

    // 응답 대기를 위한 임시 저장소 (Command ID -> 응답 데이터)
    private final Map<String, String> pendingResponses = new ConcurrentHashMap<>();

    private static final Random RANDOM = new Random();
    private static final long TIMEOUT_MS = 5000; // 5초

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

    /**
     * Service에서 호출: 엣지에 테스트 명령을 보내고 응답을 동기적으로 기다립니다.
     */
    public String requestTestAndGetResponse(String deviceSerial) throws Exception {
        WebSocketSession session = getSession(deviceSerial);
        if (session == null || !session.isOpen()) {
            throw new IllegalStateException("장치 [" + deviceSerial + "]의 WebSocket 연결이 활성화되어 있지 않습니다.");
        }

        // 1. Command ID 및 메시지 생성
        String commandId = deviceSerial + "-" + System.currentTimeMillis() + "-" + RANDOM.nextInt(1000);
        String testCommand = "{\"type\": \"TEST_REQUEST\", \"commandId\": \"" + commandId + "\"}";

        try {
            // 2. 메시지 전송
            session.sendMessage(new TextMessage(testCommand));
        } catch (IOException e) {
            throw new RuntimeException("WebSocket 메시지 전송 실패: " + e.getMessage());
        }

        // 3. 응답 대기 (동기적 처리)
        return waitForResponse(commandId, TIMEOUT_MS);
    }

    /**
     * WebSocketHandler에서 호출: 엣지 응답을 설정하고 대기 스레드를 깨웁니다.
     */
    public void setResponse(String commandId, String responseData) {
        synchronized (this) {
            pendingResponses.put(commandId, responseData);
            this.notifyAll();
        }
    }

    /**
     * Command ID에 대한 응답이 올 때까지 스레드를 대기시킵니다.
     */
    private String waitForResponse(String commandId, long timeoutMs) throws InterruptedException, TimeoutException {
        long startTime = System.currentTimeMillis();

        while (!pendingResponses.containsKey(commandId) && System.currentTimeMillis() - startTime < timeoutMs) {
            synchronized (this) {
                this.wait(50);
            }
        }

        String response = pendingResponses.remove(commandId);
        if (response == null) {
            throw new TimeoutException("엣지 장치로부터 응답을 받는 데 시간 초과(" + timeoutMs + "ms)가 발생했습니다.");
        }
        return response;
    }
}