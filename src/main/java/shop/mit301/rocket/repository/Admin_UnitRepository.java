package shop.mit301.rocket.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import shop.mit301.rocket.domain.Unit;

import java.util.Optional;

public interface Admin_UnitRepository extends JpaRepository<Unit, Integer> {
    // UI에서 문자 단위를 받아 PK(int)로 변경할 때 사용
    Optional<Unit> findByUnit(String unitName);
}
