package io.github.j_yuhanwang.food_ordering_app.payment.services;

import com.stripe.Stripe;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.model.PaymentIntent;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import com.stripe.param.checkout.SessionCreateParams;
import io.github.j_yuhanwang.food_ordering_app.auth_users.entity.User;
import io.github.j_yuhanwang.food_ordering_app.auth_users.services.UserService;
import io.github.j_yuhanwang.food_ordering_app.canteen.entity.Canteen;
import io.github.j_yuhanwang.food_ordering_app.canteen.repository.CanteenRepository;
import io.github.j_yuhanwang.food_ordering_app.enums.OrderStatus;
import io.github.j_yuhanwang.food_ordering_app.enums.PaymentGateway;
import io.github.j_yuhanwang.food_ordering_app.enums.PaymentStatus;
import io.github.j_yuhanwang.food_ordering_app.exceptions.BadRequestException;
import io.github.j_yuhanwang.food_ordering_app.exceptions.ResourceNotFoundException;
import io.github.j_yuhanwang.food_ordering_app.order.entity.Order;
import io.github.j_yuhanwang.food_ordering_app.order.repository.OrderRepository;
import io.github.j_yuhanwang.food_ordering_app.order.services.OrderService;
import io.github.j_yuhanwang.food_ordering_app.payment.dtos.PaymentDTO;
import io.github.j_yuhanwang.food_ordering_app.payment.entity.Payment;
import io.github.j_yuhanwang.food_ordering_app.payment.mapper.PaymentMapper;
import io.github.j_yuhanwang.food_ordering_app.payment.repository.PaymentRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

/**
 * @author YuhanWang
 * @Date 10/04/2026 8:28 pm
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {
    private final OrderRepository orderRepository;
    private final OrderService orderService;
    private final PaymentRepository paymentRepository;
    private final PaymentMapper paymentMapper;
    private final UserService userService;
    private final CanteenRepository canteenRepository;

    @Value("${stripe.api.secret.key}")
    private String secretKey; //key to create checkout session

    @Value("${frontend.base.url}")
    private String frontendBaseUrl;

    @Value("${stripe.webhook.secret}")
    private String webhookSecret; //Key to receive callbacks and prevent forgery

    //After Spring injects the secretKey, it is immediately handed over to the Stripe global configuration.
    @PostConstruct
    public void init() {
        Stripe.apiKey = secretKey;
        log.info("Stripe SDK initialized successfully.");
    }

    @Override
    @Transactional
    public String createCheckoutSession(Long orderId) throws StripeException {
        //1.fetch & validate
        log.info("Attempting to create check session for order: {}", orderId);
        Order order = orderRepository.findById(orderId).orElseThrow(
                () -> new ResourceNotFoundException("Order", "id", orderId)
        );
        // 2. State Machine Guard: only PENDING orders are allowed to proceed
        PaymentStatus paymentStatus = order.getPaymentStatus();
        OrderStatus orderStatus = order.getOrderStatus();
        // 2.1. Final state interception: Completed or refunded
        if (paymentStatus == PaymentStatus.COMPLETED) {
            throw new BadRequestException("Order #" + orderId + " has already been paid.");
        }
        if (paymentStatus == PaymentStatus.REFUNDED) {
            throw new BadRequestException("Order #" + orderId + " has been refunded and cannot be re-paid.");
        }

        // 2.2. Orders Cancelled or Historical Payments Completely Failed
        // For cancelled orders, physical resources (such as locked inventory) have been released.
        // The front end must be forced to guide students to re-order instead of continuing to pay for the dead order.
        if (orderStatus == OrderStatus.CANCELLED || paymentStatus == PaymentStatus.FAILED) {
            throw new BadRequestException("Order #" + orderId + " is cancelled or failed. Please create a new order.");
        }

        // 2.3. Fallback Defense: Only pure PENDING states are allowed to pass.
        if (paymentStatus != PaymentStatus.PENDING) {
            throw new BadRequestException("Invalid payment status for checkout: " + paymentStatus);
        }

        //3.Local Persistence - insert the payment data in database
        //If the database still contains old/unpaid payment for current order,
        // proceed with the payment instead of creating a new payment.
        Payment savedPayment;
        Optional<Payment> existingPendingPayment = paymentRepository.findByOrderIdAndPaymentStatus(orderId, PaymentStatus.PENDING);
        if (existingPendingPayment.isPresent()) {
            savedPayment = existingPendingPayment.get();
            log.info("Reused existing PENDING payment ID: {} for Order: {}", savedPayment.getId(), orderId);
        } else {
            Payment payment = Payment.builder()
                    .user(order.getUser())
                    .order(order)
                    .amount(order.getTotalAmount())
                    .paymentStatus(PaymentStatus.PENDING)
                    .paymentGateway(PaymentGateway.STRIPE)
                    .build();
            //save at first to get the payment id,later bind it to the stripe
            savedPayment = paymentRepository.save(payment);
            log.info("Created new PENDING payment ID: {} for Order: {}", savedPayment.getId(), orderId);
        }

        //4.Build Stripe Payload
        //Floating-point numbers have precision issues, and doubles cannot be used when dealing with real money.
        // Global payment systems uniformly use integers to represent the smallest unit of currency (cents).
        long amountInCents = order.getTotalAmount().multiply(new BigDecimal("100")).longValue();
        SessionCreateParams params = SessionCreateParams.builder()
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setSuccessUrl(frontendBaseUrl + "/payment/success?orderId=" + orderId) //temporary placeholder
                .setCancelUrl(frontendBaseUrl + "/cart")//temporary placeholder
                .addLineItem(SessionCreateParams.LineItem.builder()
                        .setQuantity(1L)
                        .setPriceData(SessionCreateParams.LineItem.PriceData.builder()
                                .setCurrency("eur")
                                .setUnitAmount(amountInCents)
                                .setProductData(SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                        .setName("UCD Canteen Order #" + orderId)
                                        .build())
                                .build())
                        .build())
                //By default, session metadata is not automatically copied to the PaymentIntent.
                //Metadata for payment failed situation.
                .setPaymentIntentData(
                        SessionCreateParams.PaymentIntentData.builder()
                                .putMetadata("paymentId", savedPayment.getId().toString())
                                .putMetadata("orderId", orderId.toString())
                                .build()
                )
                //Metadata for session expired situation
                .putMetadata("paymentId", savedPayment.getId().toString())
                .putMetadata("orderId", orderId.toString())
                .build();

        //5.Execute & Return
        Session session = Session.create(params);
        log.info("Stripe Session URL created for Order: {}", orderId);

        return session.getUrl();
    }

    /**
     * Asynchronous callback: Verify signature & update Payment to COMPLETED
     *
     * @param payload   The raw data carrier of a webhook. It contains the complete JSON message of the Stripe event.
     * @param signature Signature=HMAC-SHA256(key= webhookSecret, message = t +'.'+ rawPayload)
     */
    @Override
    @Transactional
    public void processStripeWebhook(String payload, String signature) {
        log.info("Attempting to process Stripe Webhook");
        Event event;
        //1.Verifying Stripe Signatures
        try {
            event = Webhook.constructEvent(payload, signature, webhookSecret);
        } catch (SignatureVerificationException e) {
            log.error("Webhook signature verification failed.");
            throw new BadRequestException("Invalid signature");
        }

        String eventType = event.getType();
        log.info("Received Stripe Event Type: {}", eventType);

        //2.Processing payment success events
        if ("checkout.session.completed".equals(eventType)) {
            handlePaymentSuccess(event);
        } else if ("checkout.session.expired".equals(eventType)) {
            handleSessionExpired(event);
        } else if ("payment_intent.payment_failed".equals(eventType)) {
            handlePaymentIntentFailed(event);
        } else if ("charge.refunded".equals(eventType)) {
            //The reserved refund interface can currently only print logs.
            log.info("Payment has been refunded.");
        }
    }

    //Method A: Specifically handle success payment
    private void handlePaymentSuccess(Event event) {
        Session session = (Session) event.getDataObjectDeserializer().getObject().orElse(null);
        if (session == null) {
            log.error("Failed to deserialize Session object from event: {}", event.getId());
            return;
        }
        //3. Retrieve the payment ID and order ID stored in the metadata for callback purposes.
        Long paymentId = Long.parseLong(session.getMetadata().get("paymentId"));
        Long orderId = Long.parseLong(session.getMetadata().get("orderId"));

        //4.update Payment entity
        Payment payment = paymentRepository.findById(paymentId).orElseThrow(
                () -> new ResourceNotFoundException("Payment", "paymentId", paymentId)
        );
        //Idempotency defense: If the payment status is already COMPLETED/FAILED, return directly.
        if (payment.getPaymentStatus() == PaymentStatus.COMPLETED || payment.getPaymentStatus() == PaymentStatus.FAILED) {
            log.warn("Idempotency check triggered: Payment {} already in terminal state, skipping.", paymentId);
            return;
        }
        payment.setPaymentStatus(PaymentStatus.COMPLETED);
        payment.setTransactionId(session.getPaymentIntent()); //Record the actual serial number of Stripe
        payment.setPaymentDate(LocalDateTime.now()); // record the current time
        paymentRepository.save(payment);

        //5. update order status
        orderService.updateOrderStatus(orderId, OrderStatus.CONFIRMED);

        log.info("Payment {} verified and Order {} confirmed.", paymentId, orderId);

    }

    //Method B: Specifically handle Session expiration
    private void handleSessionExpired(Event event) {
        Session session = (Session) event.getDataObjectDeserializer().getObject().orElse(null);

        if (session == null || session.getMetadata() == null || !session.getMetadata().containsKey("paymentId")) {
            log.warn("Session expired event missing metadata, skipping.");
            return;
        }
        Long paymentId = Long.parseLong(session.getMetadata().get("paymentId"));
        Long orderId = Long.parseLong(session.getMetadata().get("orderId"));
        executeFailureStateTransition(paymentId, orderId, "Session expired");
    }

    //Method C: Specifically handles failed deductions
    private void handlePaymentIntentFailed(Event event) {
        PaymentIntent intent = (PaymentIntent) event.getDataObjectDeserializer().getObject().orElse(null);
        if (intent == null || intent.getMetadata() == null || !intent.getMetadata().containsKey("paymentId")) {
            log.warn("Payment event missing metadata, skipping.");
            return;
        }
        Long paymentId = Long.parseLong(intent.getMetadata().get("paymentId"));
        Long orderId = Long.parseLong(intent.getMetadata().get("orderId"));
        executeFailureStateTransition(paymentId, orderId, "Card Declined / Payment Failed");
    }

    //Helper function for payment handler method B&C: Core state reversal engine (reusable logic)
    private void executeFailureStateTransition(Long paymentId, Long orderId, String reason) {
        Payment payment = paymentRepository.findById(paymentId).orElseThrow(
                () -> new ResourceNotFoundException("Payment", "paymentId", paymentId)
        );
        //Idempotency defense: If the payment status is already COMPLETED/FAILED, return directly.
        if (payment.getPaymentStatus() == PaymentStatus.COMPLETED || payment.getPaymentStatus() == PaymentStatus.FAILED) {
            log.warn("Idempotency check triggered: Payment {} already in terminal state, skipping.", paymentId);
            return;
        }

        payment.setPaymentStatus(PaymentStatus.FAILED);
        paymentRepository.save(payment);

        orderService.updateOrderStatus(orderId, OrderStatus.CANCELLED);
        log.warn("Payment {} and Order {} marked as FAILED. Reason: {}", paymentId, orderId, reason);
    }

    //Get all payments for ADMIN
    @Override
    public Page<PaymentDTO> getAllPayments(int page, int size) {
        log.info("Admin attempting to get all payments in system");
        Pageable pageable = createPageRequest(page, size);
        Page<Payment> payments = paymentRepository.findAll(pageable);
        return payments.map(paymentMapper::toDTO);
    }

    //Get specific canteen payments for ADMIN/canteen's manager
    @Override
    public Page<PaymentDTO> getPaymentsByCanteenId(int page, int size, Long canteenId) {
        log.info("Attempting to get payments for canteenId: {}", canteenId);
        //fetch current user and its role
        User currentUser = userService.getCurrentLoggedInUser();
        boolean isAdmin = currentUser.isAdmin();

        if (!isAdmin) {
            Canteen canteen = canteenRepository.findByIdAndIsDeletedFalse(canteenId).orElseThrow(
                    () -> new ResourceNotFoundException("Canteen", "canteen id", canteenId)
            );
            //Core comparison: Extract the cafeteria's manager_id and compare it with the current user's ID.
            // If the canteen has not yet been assigned a manager, access is directly denied.
            if (canteen.getManager() == null || !canteen.getManager().getId().equals(currentUser.getId())) {
                log.warn("IDOR Blocked: User {} attempted to access Canteen {}'s payments.", currentUser.getId(), canteenId);
                throw new BadRequestException("Access Denied: You do not manage this canteen.");
            }
        }
        Pageable pageable = createPageRequest(page, size);
        Page<Payment> payments = paymentRepository.findByCanteenId(canteenId, pageable);
        return payments.map(paymentMapper::toDTO);
    }

    //Admin can query all orders of a specific user
    @Override
    public Page<PaymentDTO> getPaymentsByTargetUserId(int page, int size, Long targetUserId) {
        log.info("Admin/Manager attempting to get payments for target userId: {}", targetUserId);
        Pageable pageable = createPageRequest(page, size);
        Page<Payment> payments = paymentRepository.findByUserId(targetUserId, pageable);
        return payments.map(paymentMapper::toDTO);
    }

    //User checks their own order
    @Override
    public Page<PaymentDTO> getPaymentsByUser(int page, int size) {
        User user = userService.getCurrentLoggedInUser();
        log.info("Attempting to get payments for current user: {}", user.getId());
        Pageable pageable = createPageRequest(page, size);
        Page<Payment> payments = paymentRepository.findByUserId(user.getId(), pageable);

        return payments.map(paymentMapper::toDTO);
    }

    //Precise single order query (authorization check)
    @Override
    public PaymentDTO getPaymentByOrderId(Long orderId) {
        log.info("Attempting to get payment by orderId: {}", orderId);
        Payment payment = paymentRepository.findByOrderId(orderId).orElseThrow(
                () -> new ResourceNotFoundException("Payment", "orderId", orderId)
        );
        //Data Ownership Guard
        User user = userService.getCurrentLoggedInUser();
        boolean isAdmin = user.isAdmin();
        boolean isOwner = payment.getUser().getId().equals(user.getId());

        if (!isAdmin && !isOwner) {
            User canteenManager = payment.getOrder().getCanteen().getManager();
            boolean isCanteenManager = canteenManager != null && canteenManager.getId().equals(user.getId());
            if (!isCanteenManager) {
                log.warn("IDOR attempt blocked. User {} tried to access payment for order {}", user.getId(), orderId);
                throw new BadRequestException("Access Denied: You do not have permission to view this payment.");
            }
        }

        return paymentMapper.toDTO(payment);
    }

    private Pageable createPageRequest(int page, int size) {
        // Standardize parameters and sort them in descending order by ID (newest first) by default.
        return PageRequest.of(
                Math.max(0, page),
                Math.max(1, Math.min(size, 100)),
                Sort.by(Sort.Direction.DESC, "id")
        );
    }
}
