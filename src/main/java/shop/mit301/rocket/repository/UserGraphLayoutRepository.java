package shop.mit301.rocket.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import shop.mit301.rocket.domain.UserGraphLayout;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserGraphLayoutRepository extends JpaRepository<UserGraphLayout, Long> {
    List<UserGraphLayout> findByUserId(String userId);
    Optional<UserGraphLayout> findByUserIdAndDragId(String userId, String dragId);
}