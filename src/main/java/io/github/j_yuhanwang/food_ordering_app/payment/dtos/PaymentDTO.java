package io.github.j_yuhanwang.food_ordering_app.payment.dtos;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.github.j_yuhanwang.food_ordering_app.enums.PaymentGateway;
import io.github.j_yuhanwang.food_ordering_app.enums.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * * Data Transfer Object representing a financial transaction.
 * * This class flattens complex relationships (User, Order, Canteen) to provide
 * a lightweight "receipt" view for the frontend.
 *
 * @author YuhanWang
 * @Date 02/02/2026 7:37 pm
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PaymentDTO {
    private Long id;

    //flattened structure of reference
    private Long userId;
    private String userName;
    private Long orderId;

    /**
     * Contextual information from the Order's Canteen.
     * Helps users identify the payment source (e.g., "Main Restaurant").
     */
    private String canteenName;

    private BigDecimal amount;

    private PaymentStatus paymentStatus;

    //Helper field for UI. True if status is COMPLETED.
    private boolean isSuccess;

    //Gateway Details
    private String transactionId;
    private PaymentGateway paymentGateway;
    private String failureReasons;

    //The actual time when money was successfully moved.
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime paymentDate;

    //The time when the payment intent was first created.
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;



}
