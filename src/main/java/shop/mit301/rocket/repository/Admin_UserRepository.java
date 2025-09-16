package shop.mit301.rocket.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import shop.mit301.rocket.domain.User;

import java.util.Optional;

public interface Admin_UserRepository extends JpaRepository <User, String> {
    // 이메일 중복 체크용
    Optional<User> findByEmail(String email);
    // 전화번호 중복 체크용
    Optional<User> findByTel(String tel);
}
