package io.github.j_yuhanwang.food_ordering_app.order.repository;

import io.github.j_yuhanwang.food_ordering_app.order.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Repository for individual items within a completed order.
 *
 * @author YuhanWang
 * @Date 28/01/2026 9:36 pm
 */
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
    /**
     * Checks if a specific dish was part of a specific order.
     * * Usage:
     * scenario1: a user wants to write a review for a dish, but the system needs to check whether he/she have ordered it before
     * scenario2: a user wants to add a dish to the cart, the system needs to check whether he/she have ordered it before.
     *            If it is, add the number, else add the whole info of that dish.
     *
     * @param orderId ID of the transaction.
     * @param dishId ID of the dish.
     * @return true if the dish exists in the order, false otherwise.
     */
    @Query("SELECT CASE WHEN COUNT(oi) > 0 THEN true ELSE false END " +
            "FROM OrderItem oi " +
            "WHERE oi.order.id = :orderId AND oi.dish.id = :dishId")
    boolean existsByOrderIdAndDishId(
            @Param("orderId") Long orderId,
            @Param("dishId") Long dishId
    );
}
