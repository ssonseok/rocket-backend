package shop.mit301.rocket.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import shop.mit301.rocket.domain.User;
import shop.mit301.rocket.dto.UserRegisterDTO;
import shop.mit301.rocket.repository.Admin_UserRepository;

@Service
@RequiredArgsConstructor
public class Admin_UserServiceImpl implements Admin_UserService {

    private final Admin_UserRepository adminUserRepository;

    @Override
    public String registerUser(UserRegisterDTO dto) {

        // ID 중복 체크
        if(adminUserRepository.existsById(dto.getUserId())) {
            return "duplicateId";
        }
        // 이메일 중복 체크
        if(adminUserRepository.findByEmail(dto.getEmail()).isPresent()) {
            return "duplicateEmail";
        }
        // 전화번호 중복 체크
        if(adminUserRepository.findByTel(dto.getTel()).isPresent()) {
            return "duplicateTel";
        }

        // Builder로 엔티티 생성
        User user = User.builder()
                .userid(dto.getUserId())
                .pw(dto.getPw())
                .name(dto.getName())
                .email(dto.getEmail())
                .tel(dto.getTel())
                .permission((byte) dto.getPermission())
                .build();

        adminUserRepository.save(user);

        return "success";
    }
}

