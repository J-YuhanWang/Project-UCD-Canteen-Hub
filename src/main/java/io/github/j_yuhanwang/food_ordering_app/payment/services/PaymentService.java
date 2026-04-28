package io.github.j_yuhanwang.food_ordering_app.payment.services;

import com.stripe.exception.StripeException;
import io.github.j_yuhanwang.food_ordering_app.payment.dtos.PaymentDTO;
import org.springframework.data.domain.Page;

/**
 * @author YuhanWang
 * @Date 10/04/2026 7:03 pm
 */
public interface PaymentService {
    String createCheckoutSession(Long orderId) throws StripeException;
    void processStripeWebhook(String payload, String signature);

    Page<PaymentDTO> getAllPayments(int page, int size);
    PaymentDTO getPaymentByOrderId(Long orderId);
    Page<PaymentDTO> getPaymentsByCanteenId(int page,int size,Long canteenId);
    Page<PaymentDTO> getPaymentsByTargetUserId(int page,int size,Long userId);
    Page<PaymentDTO> getPaymentsByUser(int page,int size);
}
