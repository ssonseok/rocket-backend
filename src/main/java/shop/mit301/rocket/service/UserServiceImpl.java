package shop.mit301.rocket.service;

import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import shop.mit301.rocket.domain.User;
import shop.mit301.rocket.repository.UserRepository;


@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final JavaMailSender mailSender;

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
}
