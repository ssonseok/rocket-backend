package shop.mit301.rocket.repository;

import shop.mit301.rocket.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, String> {

    // 이메일 중복 체크용
    Optional<User> findByEmail(String email);

    // 전화번호 중복 체크용
    Optional<User> findByTel(String tel);
}

