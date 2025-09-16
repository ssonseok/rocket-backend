package shop.mit301.rocket.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User_has_DeviceDataDTO {
    private String userId;
    private int deviceDataId;
}
