package shop.mit301.rocket.service;


import shop.mit301.rocket.domain.User;
import shop.mit301.rocket.dto.UserRegisterDTO;

public interface UserService {
    String registerUser(UserRegisterDTO dto);
    void sendUserId(String email);
    void sendPasswordResetLink(String email);

}



