package io.github.j_yuhanwang.food_ordering_app.payment.repository;

import io.github.j_yuhanwang.food_ordering_app.enums.PaymentStatus;
import io.github.j_yuhanwang.food_ordering_app.payment.entity.Payment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Repository interface for Payment entity.
 *
 * @author YuhanWang
 * @Date 28/01/2026 6:14 pm
 */
public interface PaymentRepository extends JpaRepository<Payment,Long> {
    Page<Payment> findByUserId(Long userId, Pageable pageable);
    Optional<Payment> findByOrderId(Long orderId);
    Optional<Payment> findByOrderIdAndPaymentStatus(Long orderId, PaymentStatus paymentStatus);
}
