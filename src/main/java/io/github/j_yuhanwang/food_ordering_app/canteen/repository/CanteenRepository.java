package io.github.j_yuhanwang.food_ordering_app.canteen.repository;

import io.github.j_yuhanwang.food_ordering_app.canteen.entity.Canteen;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository interface for canteen entities.
 * Serves as the primary entry point for managing dining facilities and their schedules.
 *
 * @author YuhanWang
 * @Date 28/01/2026 2:49 pm
 */
public interface CanteenRepository extends JpaRepository<Canteen,Long> {
}
