package io.github.j_yuhanwang.food_ordering_app.payment.controller;

import com.stripe.exception.StripeException;
import io.github.j_yuhanwang.food_ordering_app.payment.dtos.PaymentDTO;
import io.github.j_yuhanwang.food_ordering_app.payment.services.PaymentService;
import io.github.j_yuhanwang.food_ordering_app.response.Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * @author YuhanWang
 * @Date 27/04/2026 6:38 pm
 */
@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
@Slf4j
public class PaymentController {
    private final PaymentService paymentService;

    @PostMapping("/checkout/{orderId}")
    public Response<String> createCheckoutSession(@PathVariable Long orderId) throws StripeException {
        log.info("REST request to create checkout session for order: {}", orderId);
        String sessionUrl = paymentService.createCheckoutSession(orderId);
        return Response.ok(sessionUrl);
    }

    @PostMapping("/webhook")
    public Response<Void> processStripeWebhook(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String signature) {
        log.info("Stripe Webhook received");
        paymentService.processStripeWebhook(payload, signature);
        return Response.ok();
    }

    @GetMapping("/all")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public Response<Page<PaymentDTO>> getAllPayments(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("Admin request to get all payments in the system");
        return Response.ok(paymentService.getAllPayments(page, size));
    }

    @GetMapping("/canteen/{canteenId}")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_MANAGER')")
    public Response<Page<PaymentDTO>> getPaymentsByCanteenId(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @PathVariable Long canteenId
    ) {
        log.info("REST request to get payments for Canteen #{}", canteenId);
        return Response.ok(paymentService.getPaymentsByCanteenId(page,size,canteenId));
    }

    @GetMapping("/user/{targetUserId}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public Response<Page<PaymentDTO>> getPaymentsByTargetUser(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @PathVariable Long targetUserId){
        log.info("REST request to get user payments history for User #{}",targetUserId);
        return Response.ok(paymentService.getPaymentsByTargetUserId(page,size,targetUserId));
    }

    @GetMapping("/my-payments")
    @PreAuthorize("hasRole('ROLE_STUDENT')")
    public Response<Page<PaymentDTO>> getMyPayments(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size){
        log.info("REST request to get personal payments history");
        return Response.ok(paymentService.getPaymentsByUser(page,size));
    }

    @GetMapping("/order/{orderId}")
    @PreAuthorize("isAuthenticated()")
    public Response<PaymentDTO> getPaymentByOrderId(@PathVariable Long orderId) {
        log.info("REST request to get payment details for Order #{}", orderId);
        return Response.ok(paymentService.getPaymentByOrderId(orderId));
    }


}
