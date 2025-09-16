package shop.mit301.rocket.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import shop.mit301.rocket.domain.User;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, String> {
    Optional<User> findByUseridAndPw(String userid, String pw);
    Optional<User> findByEmail(String email);
    Optional<User> findByUseridAndEmail(String userid, String email);
}
