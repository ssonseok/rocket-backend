package shop.mit301.rocket.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import shop.mit301.rocket.domain.PasswordResetToken;
import shop.mit301.rocket.domain.User;
import shop.mit301.rocket.repository.Admin_UserRepository;
import shop.mit301.rocket.repository.PasswordResetTokenRepository;

import java.time.LocalDateTime;
import java.util.UUID;


@Service
@RequiredArgsConstructor
@Log4j2
public class UserServiceImpl implements UserService {

    private final Admin_UserRepository userRepository;
    private final JavaMailSender mailSender;
    private final PasswordResetTokenRepository tokenRepository;

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
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("해당 이메일의 사용자가 존재하지 않습니다."));

        String resetToken = UUID.randomUUID().toString();

        PasswordResetToken tokenEntity = PasswordResetToken.builder()
                .token(resetToken)
                .user(user)
                .expiryDate(LocalDateTime.now().plusMinutes(30))
                .build();

        tokenRepository.save(tokenEntity);

        String link = "https://rocket.mit301.shop/changePwLink.html?token=" + resetToken;
        String subject = "비밀번호 재설정 링크";
        String body = "다음 링크를 클릭하여 비밀번호를 재설정하세요:\n" + link;

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject(subject);
        message.setText(body);

        try {
            mailSender.send(message);
        } catch (MailException e) {
            log.error("메일 전송 실패: {}", e.getMessage());
            throw new RuntimeException("메일 전송 실패");
        }
    }

}
