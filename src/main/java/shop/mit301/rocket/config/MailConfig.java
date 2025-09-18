package shop.mit301.rocket.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

@Configuration
public class MailConfig {

    @Bean
    public JavaMailSender javaMailSender() {
        // 실제 SMTP 연결 없이 빈 껍데기 Bean만 제공
        return new JavaMailSenderImpl();
    }
}
