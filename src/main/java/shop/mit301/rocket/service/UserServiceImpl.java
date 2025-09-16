package shop.mit301.rocket.service;

import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import shop.mit301.rocket.domain.PasswordResetToken;
import shop.mit301.rocket.domain.User;
import shop.mit301.rocket.dto.UserDTO;
import shop.mit301.rocket.repository.PasswordResetTokenRepository;
import shop.mit301.rocket.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.UUID;


@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final JavaMailSender mailSender;
    private final PasswordResetTokenRepository tokenRepository;
    private final EmailService emailService;

    public void sendUserId(UserDTO userDTO) {
        String email = userDTO.getEmail();
        String userId = userDTO.getUserid();  // 아이디 포함되어 있다고 가정

        String subject = "[로켓샵] 아이디 찾기 결과 안내";
        String text = "회원님의 아이디는 다음과 같습니다:\n\n" +
                "아이디: " + userId + "\n\n" +
                "감사합니다.";

        emailService.send(email, subject, text);
    }

    @Override
    public void sendPasswordResetLink(UserDTO userDTO) {
        String token = UUID.randomUUID().toString();

        User user = userRepository.findById(userDTO.getUserid()).orElseThrow();

        PasswordResetToken resetToken = new PasswordResetToken();
        resetToken.setToken(token);
        resetToken.setUser(user);
        resetToken.setExpiryDate(LocalDateTime.now().plusHours(1));

        tokenRepository.save(resetToken);

        String resetLink = "http://your-frontend-url.com/changePwLink?token=" + token;

        emailService.send(
                user.getEmail(),
                "[로켓샵] 비밀번호 재설정 링크",
                "아래 링크를 클릭하여 비밀번호를 재설정하세요:\n" + resetLink
        );
    }
}
