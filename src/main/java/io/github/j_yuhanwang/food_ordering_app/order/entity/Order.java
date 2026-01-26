package io.github.j_yuhanwang.food_ordering_app.order.entity;

import io.github.j_yuhanwang.food_ordering_app.auth_users.entity.User;
import io.github.j_yuhanwang.food_ordering_app.enums.OrderStatus;
import io.github.j_yuhanwang.food_ordering_app.enums.PaymentStatus;
import io.github.j_yuhanwang.food_ordering_app.payment.entity.Payment;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a confirmed purchase transaction in the system.
 * <p>
 * The Order entity acts as the <b>Aggregate Root</b> for the transaction lifecycle.
 * It manages the lifecycle of its {@link OrderItem}s and links the {@link User}
 * with the corresponding {@link Payment}.
 *
 * @author BlairWang
 * @version 1.0
 * @date 26/01/2026 8:38 am
 */

@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "orders")
@Builder
@EntityListeners(AuditingEntityListener.class)
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The customer who placed this order.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @ToString.Exclude
    private User user;

    /**
     * The timestamp when the order was created.
     * Automatically populated by Spring Data JPA Auditing.
     * Immutable (updatable = false).
     */
    @CreatedDate
    @Column(updatable = false, nullable = false)
    private LocalDateTime orderDate;

    /**
     * The final total cost of the order.
     * Uses BigDecimal for financial precision.
     */
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount;

    /**
     * The fulfillment status of the order (e.g., INITIALIZED, CANCELLED, DELIVERED).
     */
    @Enumerated(EnumType.STRING)
    private OrderStatus orderStatus;

    /**
     * Redundant storage of payment status for quick access (Query Optimization).
     * The authoritative status is stored in the linked {@link Payment} entity.
     */
    @Enumerated(EnumType.STRING)
    private PaymentStatus paymentStatus;

    /**
     * The financial record associated with this order.
     * One-to-One relationship where the Payment holds the foreign key.
     */
    @OneToOne(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @ToString.Exclude
    private Payment payment;

    /**
     * The specific list of dishes included in this order.
     * <p>
     * <b>orphanRemoval = true:</b> Essential for data integrity. If an item is removed
     * from this list programmatically, it is strictly deleted from the database.
     */
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    @ToString.Exclude
    private List<OrderItem> orderItems = new ArrayList<>();

}
