package shop.mit301.rocket.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import shop.mit301.rocket.websocket.EdgeWebSocketHandler;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private final EdgeWebSocketHandler handler;

    public WebSocketConfig(EdgeWebSocketHandler handler) {
        this.handler = handler;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry//.addHandler(handler, "/ws/edge")
                .addHandler(handler, "/ws")
                .setAllowedOriginPatterns("*");
    }
}
