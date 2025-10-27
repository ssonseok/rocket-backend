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
public class Admin_DeviceDetailRespDTO {
    private String deviceSerialNumber;
    private String name;
    private String edgeSerial;
    private String edgeIp;
    private int edgePort;
    private List<Admin_DeviceDataDetailRespDTO> deviceDataList;
}
