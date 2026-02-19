package io.github.j_yuhanwang.food_ordering_app.payment.entity;

import io.github.j_yuhanwang.food_ordering_app.auth_users.entity.User;
import io.github.j_yuhanwang.food_ordering_app.enums.PaymentGateway;
import io.github.j_yuhanwang.food_ordering_app.enums.PaymentStatus;
import io.github.j_yuhanwang.food_ordering_app.order.entity.Order;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Represents a financial transaction record in the system.
 * This entity links a specific Order with a payment attempt.
 * It tracks the status of the payment (COMPLETED, FAILED) and stores
 * external references (Transaction ID) from gateways like Stripe or PayPal.
 *
 * @author BlairWang
 * @version 1.0
 * @date 26/01/2026 8:39 am
 */

@Builder
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "payments")
@Data
@EntityListeners(AuditingEntityListener.class)
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The order associated with this payment.
     * Relationship: One-to-One.
     * Owner Side: Payment owns the foreign key (order_id).
     * This implies an Order can exist without a Payment (temporarily),
     * but a Payment must belong to an Order.
     */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false, unique = true)
    @ToString.Exclude
    //payment has the foreign key order_id -> payment is the owner side
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @ToString.Exclude
    private User user;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    private PaymentStatus paymentStatus;

    /**
     * The unique reference ID returned by the payment gateway (e.g., Stripe PaymentIntent ID).
     * Essential for reconciliation.
     */
    @Column(unique = true)
    private String transactionId;

    @Enumerated(EnumType.STRING)
    private PaymentGateway paymentGateway;

    /**
     * Stores the error message if the payment fails (e.g., "Insufficient funds").
     */
    private String failureReasons;


    // -----------------------------------------------------------------
    // Timestamp Logic
    // -----------------------------------------------------------------

    /**
     * Creation Time (Payment Intent):
     * The timestamp when the user clicked "Pay" and the payment record was initialized (status: PENDING).
     * Used for timeout logic: If the payment is not completed within X minutes of creation,
     * the system can automatically cancel the associated order.
     */
    @CreatedDate
    @Column(updatable = false, nullable = false)
    private LocalDateTime createdAt;

    /**
     * Confirmation Time (Money Moved):
     * The timestamp when the payment was successfully processed and confirmed by the gateway.
     * This is the authoritative date used for financial reporting and accounting.
     * It remains null until the status becomes COMPLETED.
     */
    private LocalDateTime paymentDate;
}
