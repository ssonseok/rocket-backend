package shop.mit301.rocket.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeviceDTO {
    private String deviceSerialNumber;
    private int port;
    private String ip;
    private String name;
    private LocalDateTime registDate;
    private LocalDateTime modifyDate;
}
