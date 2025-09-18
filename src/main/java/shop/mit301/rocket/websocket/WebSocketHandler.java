package shop.mit301.rocket.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import shop.mit301.rocket.dto.SensorRequestDTO;
import shop.mit301.rocket.dto.SensorResponseDTO;
import shop.mit301.rocket.service.DeviceService;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class WebSocketHandler extends TextWebSocketHandler {

    private final DeviceService deviceService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) {
        try {
            SensorRequestDTO request = objectMapper.readValue(message.getPayload(), SensorRequestDTO.class);

            if ("subscribe".equalsIgnoreCase(request.getAction())) {
                List<Integer> sensorIds = request.getSensorGroups().stream()
                        .map(Integer::parseInt)
                        .toList();

                ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
                executor.scheduleAtFixedRate(() -> {
                    try {
                        List<SensorResponseDTO> responses = deviceService.collectAndSend(sensorIds);
                        for (SensorResponseDTO response : responses) {
                            session.sendMessage(new TextMessage(objectMapper.writeValueAsString(response)));
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }, 0, 1, TimeUnit.SECONDS);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
