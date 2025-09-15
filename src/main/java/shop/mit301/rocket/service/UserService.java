package shop.mit301.rocket.service;

import shop.mit301.rocket.domain.User;
import shop.mit301.rocket.dto.UserRegisterDTO;

public interface UserService {
    User registerUser(UserRegisterDTO dto) throws Exception;
}