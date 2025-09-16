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
    private final ModelMapper modelMapper;
    private final JavaMailSender mailSender;

    @Override
    public String registerUser(UserRegisterDTO dto) {
        // ID 중복
        if(userRepository.existsById(dto.getUserId())) {
            return "duplicateId";
        }
        // 이메일 중복
        if(userRepository.findByEmail(dto.getEmail()).isPresent()) {
            return "duplicateEmail";
        }
        // 전화번호 중복
        if(userRepository.findByTel(dto.getTel()).isPresent()) {
            return "duplicateTel";
        }

        // 중복 없으면 저장
        User user = modelMapper.map(dto, User.class);
        user.setUserid(dto.getUserId()); // 반드시 ID 세팅
        user.setPermission((byte) dto.getPermission());
        userRepository.save(user);

        return "success";
    }

    @Override
    public void sendUserId(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("해당 이메일을 가진 사용자가 없습니다."));

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("아이디 찾기 결과");
        message.setText("당신의 아이디는: " + user.getName());

        mailSender.send(message);
    }

    @Override
    public void sendPasswordResetLink(String email) {
        String resetToken = UUID.randomUUID().toString();
        String link = "https://rocket.mit301.shop/changePw.html?token=" + resetToken;
        String subject = "비밀번호 재설정 링크";
        String body = "다음 링크를 클릭하여 비밀번호를 재설정하세요:\n" + link;

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject(subject);
        message.setText(body);

        mailSender.send(message);
    }
}

