package shop.mit301.rocket.service;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import shop.mit301.rocket.domain.User;
import shop.mit301.rocket.dto.UserRegisterDTO;
import shop.mit301.rocket.repository.UserRepository;

import shop.mit301.rocket.service.UserService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    public String registerUser(UserRegisterDTO dto) {

        // ID 중복 체크
        if(userRepository.existsById(dto.getUserId())) {
            return "duplicateId";
        }
        // 이메일 중복 체크
        if(userRepository.findByEmail(dto.getEmail()).isPresent()) {
            return "duplicateEmail";
        }
        // 전화번호 중복 체크
        if(userRepository.findByTel(dto.getTel()).isPresent()) {
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

        userRepository.save(user);

        return "success";
    }

}

