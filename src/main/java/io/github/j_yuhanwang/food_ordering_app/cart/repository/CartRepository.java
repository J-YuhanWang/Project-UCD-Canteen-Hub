package io.github.j_yuhanwang.food_ordering_app.cart.repository;

import io.github.j_yuhanwang.food_ordering_app.cart.entity.Cart;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Repository interface for managing cart entities.
 * Handles the retrieval of a user's active shopping session.
 * Since a user typically has only one active cart, this repository focuses on linking the cart to the user ID.
 *
 * @author YuhanWang
 * @date 28/01/2026 5:53 pm
 */
public interface CartRepository extends JpaRepository<Cart,Long> {

    /**
     * Finds the active shopping cart associated with a specific user.
     *
     * @param userId The ID of the user.
     * @return An Optional containing the user's cart if it exists.
     */
    Optional<Cart> findByUserId(Long userId);


}
