package io.github.j_yuhanwang.food_ordering_app.cart.repository;

import io.github.j_yuhanwang.food_ordering_app.cart.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Repository interface for managing individual items within a shopping cart.
 *
 * @author YuhanWang
 * @Date 28/01/2026 5:55 pm
 */
public interface CartItemRepository extends JpaRepository<CartItem,Long> {

    /**
     * Checks if a specific dish item already exists in a specific cart.
     * Usage:
     * When a user adds a dish to the cart:
     * - If found: Update quantity (e.g., quantity + 1).
     * - If not found: Create a new CartItem.
     * This ensures no duplicate rows for the same dish (Unique Constraint).
     *
     * @param cartId The ID of the shopping cart.
     * @param dishId The ID of the dish item.
     * @return Optional containing the item if found.
     */
    Optional<CartItem> findByCartIdAndDishId(Long cartId, Long dishId);
}
