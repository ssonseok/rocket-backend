package shop.mit301.rocket.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GraphLayoutRequest {
    private String dragId;
    private int left;
    private int top;
    private int width;
    private int height;
}
