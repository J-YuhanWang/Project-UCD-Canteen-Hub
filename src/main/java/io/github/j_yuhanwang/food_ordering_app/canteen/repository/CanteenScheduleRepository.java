package io.github.j_yuhanwang.food_ordering_app.canteen.repository;

import io.github.j_yuhanwang.food_ordering_app.canteen.entity.CanteenSchedule;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * @author YuhanWang
 * @Date 20/03/2026 2:11 pm
 */
public interface CanteenScheduleRepository extends JpaRepository<CanteenSchedule,Long> {
}
