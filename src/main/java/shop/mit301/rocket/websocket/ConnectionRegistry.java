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
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ConnectionRegistry {

    // 1. Edge Serial을 키로 하는 활성 WebSocket 세션 맵 (Edge Serial : Session)
    private final Map<String, WebSocketSession> sessionMap = new ConcurrentHashMap<>();

    // 2. commandId를 키로 하는 응답 대기 맵 (Command ID : CompletableFuture<Response Payload>)
    private final Map<String, CompletableFuture<String>> responseFutures = new ConcurrentHashMap<>();

    // --------------------------------------------------------------------------------
    // 1. WebSocket 세션 관리 메서드
    // --------------------------------------------------------------------------------

    /**
     * Edge Serial을 키로 WebSocket 세션을 등록합니다.
     * 새로운 연결이 들어오면 기존 세션을 명시적으로 닫고 새 세션을 등록하여
     * Edge의 재연결 시 충돌을 방지합니다.
     */
    public void register(String edgeSerial, WebSocketSession newSession) {
        // 기존 세션 확인 및 정리
        WebSocketSession oldSession = sessionMap.get(edgeSerial);
        if (oldSession != null && oldSession.isOpen()) {
            System.out.println("[Registry] 기존 세션 종료 처리: " + edgeSerial);
            try {
                // 기존 세션을 닫아 Edge 애플리케이션이 새 세션으로 재연결하도록 유도
                oldSession.close(CloseStatus.POLICY_VIOLATION);
            } catch (IOException e) {
                System.err.println("[Registry] 기존 세션 종료 실패: " + e.getMessage());
            }
        }

        sessionMap.put(edgeSerial, newSession);
    }

    /**
     * Edge Serial에 해당하는 활성 WebSocket 세션을 반환합니다.
     */
    public WebSocketSession getSession(String edgeSerial) {
        return sessionMap.get(edgeSerial);
    }

    /**
     * 세션이 닫혔을 때 호출됩니다. 세션 맵에서 세션을 제거하고 Edge Serial을 반환합니다.
     */
    public String unregister(WebSocketSession session) {
        // 세션 ID로 Edge Serial을 찾습니다. (이 과정이 성능에 영향을 줄 수 있으므로 주의 필요)
        String edgeSerial = null;
        for (Map.Entry<String, WebSocketSession> entry : sessionMap.entrySet()) {
            if (entry.getValue().getId().equals(session.getId())) {
                edgeSerial = entry.getKey();
                sessionMap.remove(edgeSerial);
                break;
            }
        }

        // 닫힌 세션에 연결된 대기 중인 CompletableFuture가 있다면 예외 처리
        if (edgeSerial != null) {
            // Edge가 연결을 끊으면, 해당 Edge와 관련된 모든 요청을 실패 처리해야 함
            // (구현 복잡도 때문에 여기서는 생략하고, 다음 통신 시 타임아웃 되도록 할 수 있음)
        }

        return edgeSerial;
    }

    // --------------------------------------------------------------------------------
    // 2. 비동기 응답 동기화 메서드
    // --------------------------------------------------------------------------------

    /**
     * ServiceImpl에서 호출되어 응답 대기를 시작합니다.
     * Command ID를 키로 CompletableFuture를 생성하고 맵에 등록합니다.
     */
    public CompletableFuture<String> awaitResponse(String commandId) {
        CompletableFuture<String> future = new CompletableFuture<>();
        responseFutures.put(commandId, future);
        return future;
    }

    /**
     * EdgeWebSocketHandler에서 응답 수신 시 호출됩니다.
     * 대기 중인 CompletableFuture를 찾아 결과를 전달하고 대기를 해제합니다.
     */
    public void completeResponse(String commandId, String payload) {
        CompletableFuture<String> future = responseFutures.remove(commandId);
        if (future != null) {
            // 대기 중이던 스레드에 응답 페이로드를 전달하고 블로킹을 해제합니다.
            future.complete(payload);
        } else {
            System.err.println("[Registry] 존재하지 않는 Command ID에 대한 응답 수신: " + commandId);
        }
    }
}