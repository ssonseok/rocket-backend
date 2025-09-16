package shop.mit301.rocket.service;

import shop.mit301.rocket.dto.UserRegisterDTO;

public interface Admin_UserService {
    String registerUser(UserRegisterDTO dto);
}
