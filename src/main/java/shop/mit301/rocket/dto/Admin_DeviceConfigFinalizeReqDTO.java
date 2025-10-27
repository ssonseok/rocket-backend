package shop.mit301.rocket.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Admin_DeviceConfigFinalizeReqDTO {
    private String deviceName;
    private String deviceSerial;
    private List<Admin_DeviceDataConfigReqDTO> dataStreams;
}
