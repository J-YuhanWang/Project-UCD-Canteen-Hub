package io.github.j_yuhanwang.food_ordering_app.review.entity;

import io.github.j_yuhanwang.food_ordering_app.auth_users.entity.User;
import io.github.j_yuhanwang.food_ordering_app.menu.entity.Menu;
import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * Represents a customer's feedback and rating for a specific menu item.
 * <p>
 * Reviews help other users make decisions and provide valuable feedback to the Canteen.
 *
 * @author BlairWang
 * @version 1.0
 * @date 26/01/2026 8:39 am
 */

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "reviews")
@EntityListeners(AuditingEntityListener.class)
public class Review {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @ToString.Exclude
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "menu_id", nullable = false)
    @ToString.Exclude
    private Menu menu;

    /**
     * The numeric rating given by the user.
     * Must be an integer between 1 (Lowest) and 5 (Highest).
     */
    @Column(nullable = false)
    @Min(value = 1, message = "Rating must be at least 1")
    @Max(value = 5, message = "Rating must be at least 1")
    private Integer rating;//e.g..1 to 5 stars

    /**
     * The text content of the review.
     * Defines column as TEXT to support long feedback (up to 64KB in typical DBs),
     * avoiding the 255-character limit of standard VARCHAR.
     */
    @Column(columnDefinition = "TEXT")//Changed the input limitation from 255 characters to around 5000 characters
    private String comment;

    @CreatedDate
    @Column(updatable = false, nullable = false)
    private LocalDateTime createdAt;

    /**
     * The ID of the order associated with this review.
     * Verification Logic:
     * Storing `orderId` allows the system to verify that "Verified Purchase" condition:
     * A user can only leave a review if they have actually purchased this item.
     */
    //Logical confirmation: Storing the orderId is to verify that
    //the user can only leave a comment after they have actually purchased the order
    @Column(name = "order_id")
    private Long orderId;
}
