package io.github.j_yuhanwang.food_ordering_app.order.repository;

import io.github.j_yuhanwang.food_ordering_app.auth_users.entity.User;
import io.github.j_yuhanwang.food_ordering_app.enums.OrderStatus;
import io.github.j_yuhanwang.food_ordering_app.order.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository interface for managing Order entities.
 * Handles transaction history, status tracking, and analytics.
 *
 * @author YuhanWang
 * @Date 28/01/2026 5:48 pm
 */
public interface OrderRepository extends JpaRepository<Order, Long> {
    /**
     * Retrieves the order history for a specific user.
     */
    Page<Order> findByUserId(Long userId, Pageable pageable);

    /**
     * Retrieves orders filtered by status (e.g., PENDING, DELIVERED).
     * Performance:
     * Uses Pageable to support pagination, preventing performance issues with large datasets.
     *
     * @param orderStatus The status to filter by.
     * @param pageable Pagination info (page number, size).
     * @return A page of matching orders.
     */
    Page<Order> findByOrderStatus(OrderStatus orderStatus, Pageable pageable);

    /**
     * Calculates the number of unique customers who have placed an order.
     * Used for admin dashboard analytics.
     *
     * @return The count of distinct users.
     */
    @Query("SELECT COUNT(DISTINCT o.user.id) FROM Order o")
    long countDistinctUsers();

    List<Order> findByOrderStatusAndOrderDateBefore(OrderStatus orderStatus, LocalDateTime cutoffTime);

    @Query("SELECT SUM(o.totalAmount) " +
            "FROM Order AS o " +
            "WHERE o.orderDate BETWEEN :startDate AND :endDate " +
            "AND o.paymentStatus = 'PAID'")
    BigDecimal calculateRevenueByDateRange(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );
}
