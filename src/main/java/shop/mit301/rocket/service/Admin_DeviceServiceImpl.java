package shop.mit301.rocket.service;

import shop.mit301.rocket.dto.Admin_DeviceRegisterReqDTO;
import shop.mit301.rocket.dto.Admin_DeviceRegisterRespDTO;
import shop.mit301.rocket.repository.Admin_DeviceRepository;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

public class Admin_DeviceServiceImpl implements Admin_DeviceService{

    Admin_DeviceRepository adminDeviceRepository;

    @Override
    public boolean checkDuplicateSerialNumber(String deviceSerialNumber) {
        return adminDeviceRepository.existsByDeviceSerialNumber(deviceSerialNumber);
    }

    @Override
    public String testDeviceConnection(String ip, int port) {
        //엣지없어서 항상 연결 성공처리
        //===엣지구현되면 호출할 부분===
        return "success";
    }

    @Override
    public String registerDevice(Admin_DeviceRegisterReqDTO request) {
        return null;
    }
}
