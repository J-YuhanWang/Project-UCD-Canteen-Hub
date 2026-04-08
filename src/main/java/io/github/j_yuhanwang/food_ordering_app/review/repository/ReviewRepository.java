package io.github.j_yuhanwang.food_ordering_app.review.repository;

import io.github.j_yuhanwang.food_ordering_app.review.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * Repository for managing Review entities.
 *
 * @author YuhanWang
 * @Date 28/01/2026 10:53 pm
 */
public interface ReviewRepository extends JpaRepository<Review, Long> {
    /**
     * Finds all reviews for a specific dish, sorting them so the newest ones appear first.
     */
    Page<Review> findByDishIdOrderByCreatedAtDesc(Long dishId, Pageable pageable);

    Page<Review> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);
    /**
     * Calculates the average star rating (e.g., 4.5) for a specific dish directly in the database.
     * Used for displaying the rating badge on the dish card.
     */
    @Query("""
            SELECT AVG(r.rating)
            FROM Review r
            WHERE r.dish.id = :dishId
            """)
    Double calculateAverageRatingByDishId(@Param("dishId") Long dishId);

    /**
     * Prevents duplicate reviews.
     * Ensures a user can only leave ONE review per dish per specific order.
     */
    @Query("""
            SELECT CASE WHEN COUNT(r) > 0 THEN true ELSE false END
            FROM Review r
            WHERE r.user.id = :userId AND
                  r.dish.id=:dishId AND
                  r.orderId=:orderId
            """)
    boolean existsByUserIdAndDishIdAndOrderId(@Param("userId") Long userId, @Param("dishId") Long dishId, @Param("orderId") Long orderId);
}
