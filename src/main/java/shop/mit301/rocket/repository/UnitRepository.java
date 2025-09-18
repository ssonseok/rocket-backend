package shop.mit301.rocket.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import shop.mit301.rocket.domain.Unit;

public interface UnitRepository extends JpaRepository<Unit, Integer> {
}