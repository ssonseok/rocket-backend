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
    private final Map<String, String> edgeSerialMap = new ConcurrentHashMap<>();

    // --------------------------------------------------------------------------------
    // 1. WebSocket 세션 관리 메서드
    // --------------------------------------------------------------------------------

    /**
     * Edge Serial을 키로 WebSocket 세션을 등록합니다.
     * 새로운 연결이 들어오면 기존 세션을 명시적으로 닫고 새 세션을 등록하여
     * Edge의 재연결 시 충돌을 방지합니다.
     */
    public void register(String edgeSerial, WebSocketSession newSession) {
        WebSocketSession oldSession = sessionMap.put(edgeSerial, newSession);

        // 🚨 역방향 맵에 등록
        edgeSerialMap.put(newSession.getId(), edgeSerial);

        if (oldSession != null) {
            // 기존 세션이 있다면 역방향 맵에서도 제거 (덮어쓰기)
            edgeSerialMap.remove(oldSession.getId());
            if (oldSession.isOpen()) {
                // ... (기존 oldSession.close 로직)
            }
        }
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
        // 🚨 개선된 로직: O(1)의 성능으로 Edge Serial을 즉시 찾음
        String edgeSerial = edgeSerialMap.remove(session.getId());

        if (edgeSerial != null) {
            // 세션 맵에서도 제거
            sessionMap.remove(edgeSerial);
            // ... (대기 중인 CompletableFuture 처리 로직 추가 가능 - 현재 생략)
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
    public void removeResponse(String commandId) {
        CompletableFuture<String> future = responseFutures.remove(commandId);
        if (future != null) {
            // Future가 아직 완료되지 않았다면, 취소 처리하여 대기 중인 스레드를 깨울 수 있지만
            // 이미 checkEdgeStatus에서 TimeoutException으로 깨워졌으므로 제거만 합니다.
            future.cancel(true);
            System.out.println("[Registry] 타임아웃으로 Command ID 응답 Future 제거됨: " + commandId);
        } else {
            System.err.println("[Registry] 제거하려는 Command ID가 이미 존재하지 않음: " + commandId);
        }
    }

}