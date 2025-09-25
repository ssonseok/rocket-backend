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
public class Admin_DeviceDetailDTO {
    private String deviceSerialNumber;
    private String name;
    private String ip;
    private int port;
    private List<Admin_DeviceDataRegisterRespDTO> deviceDataList;
}
