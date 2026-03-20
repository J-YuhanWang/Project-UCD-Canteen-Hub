package io.github.j_yuhanwang.food_ordering_app.canteen.repository;

import io.github.j_yuhanwang.food_ordering_app.auth_users.entity.User;
import io.github.j_yuhanwang.food_ordering_app.canteen.entity.Canteen;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for canteen entities.
 * Serves as the primary entry point for managing dining facilities and their schedules.
 *
 * @author YuhanWang
 * @Date 28/01/2026 2:49 pm
 */
public interface CanteenRepository extends JpaRepository<Canteen,Long> {
    List<Canteen> findAllByIsDeletedFalse();

    Optional<Canteen> findByIdAndIsDeletedFalse(Long id);

    boolean existsByNameIgnoreCase(String name);

    //Prepare for assignManager
    //SELECT * FROM canteens WHERE manager_id = ? AND is_deleted = false
    Optional<Canteen> findByManagerAndDeletedIsFalse(User manager);
}
