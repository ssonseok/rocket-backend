package shop.mit301.rocket.service;

import shop.mit301.rocket.dto.UserDTO;

public interface UserService {
    void sendUserId(UserDTO userDTO);
    void sendPasswordResetLink(UserDTO userDTO);
}
