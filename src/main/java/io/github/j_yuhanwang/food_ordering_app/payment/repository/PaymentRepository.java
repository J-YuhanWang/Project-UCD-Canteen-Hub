package io.github.j_yuhanwang.food_ordering_app.payment.repository;

import io.github.j_yuhanwang.food_ordering_app.payment.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository interface for Payment entity.
 *
 * @author YuhanWang
 * @Date 28/01/2026 6:14 pm
 */
public interface PaymentRepository extends JpaRepository<Payment,Long> {
}
