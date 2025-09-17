package shop.mit301.rocket.service;

import shop.mit301.rocket.dto.Admin_UserListDTO;
import shop.mit301.rocket.dto.Admin_UserModifyDTO;
import shop.mit301.rocket.dto.UserRegisterDTO;

import java.util.List;

public interface Admin_UserService {
    //회원등록
    String registerUser(UserRegisterDTO dto);
    //회원목록
    List<Admin_UserListDTO> getAllUsers();
    //회원수정
    String modifyUser(Admin_UserModifyDTO dto);

}
