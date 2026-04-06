package io.github.j_yuhanwang.food_ordering_app.auth_users.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.github.j_yuhanwang.food_ordering_app.cart.entity.Cart;
import io.github.j_yuhanwang.food_ordering_app.enums.UserStatus;
import io.github.j_yuhanwang.food_ordering_app.order.entity.Order;
import io.github.j_yuhanwang.food_ordering_app.payment.entity.Payment;
import io.github.j_yuhanwang.food_ordering_app.review.entity.Review;
import io.github.j_yuhanwang.food_ordering_app.role.entity.Role;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
/**
 *
 * Represents a registered user in the system.
 * This entity serves as the central point for user-related data including
 * authentication credentials, orders, reviews, and payment history.
 * @author BlairWang
 * @Date 22/01/2026 8:14 pm
 * @Version 1.0
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name="users")
@Builder
@EntityListeners(AuditingEntityListener.class)
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)//auto-increment
    private Long id;

    private String name;

    // Ensures email is unique in the database and follows a valid email format
    @Column(unique = true,nullable = false)
    @Email(message = "Invalid email format")
    private String email;

    // @JsonIgnore prevents the password from being exposed in API responses (Security best practice)
    @Column(nullable = false)
    @NotBlank(message="password is required")
    @JsonIgnore
    private String password;

    private String profileUrl;

    private String phoneNumber;

    private String address;

    // Defaults to 'ACTIVE' status upon creation unless specified otherwise
    @Enumerated(EnumType.STRING)
    @Builder.Default
    @Column(name = "user_status", length = 20)
    private UserStatus userStatus = UserStatus.ACTIVE;

    // Auditing Fields
    // Automatically populated when the entity is persisted
    @CreatedDate
    @Column(updatable = false,nullable = false)
    private LocalDateTime createdAt;

    // Automatically updated whenever the entity is modified
    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updateAt;

    /**
     * Many-to-Many relationship with Role.
     * Defined as EAGER fetch to ensure roles are available during security authorization context loading.
     */
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name="user_role",
            joinColumns = @JoinColumn(name="user_id"), //user as a FK in the intermediate table
            inverseJoinColumns =@JoinColumn(name="role_id")
    )
    @Builder.Default
    @ToString.Exclude
    private List<Role> roles = new ArrayList<>();

    /**
     * One-to-Many relationship with Order.
     * 'orphanRemoval = true' ensures that if an Order is removed from this list,
     * it is strictly deleted from the database to maintain data integrity.
     */
    @OneToMany(mappedBy = "user",cascade = CascadeType.ALL,orphanRemoval = true)
    @Builder.Default
    @ToString.Exclude
    private List<Order> orders = new ArrayList<>();

    /**
     * One-to-Many relationship with Review.
     * Cascades all operations and removes orphaned reviews if disassociated from the user.
     */
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL,orphanRemoval = true)
    @Builder.Default
    @ToString.Exclude
    private List<Review> reviews = new ArrayList<>();

    /**
     * One-to-One relationship with Cart.
     * A user has exactly one active shopping cart.
     */
    @OneToOne(mappedBy = "user", fetch = FetchType.LAZY,cascade = CascadeType.ALL)
    @ToString.Exclude
    private Cart cart;

    /**
     * One-to-Many relationship with Payment.
     * Tracks the complete payment history of the user.
     */
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL,orphanRemoval = true)
    @Builder.Default
    @ToString.Exclude
    private List<Payment> payments = new ArrayList<>();

    public boolean hasRole(String roleName){
        if(this.roles == null || this.roles.isEmpty()){
            return false;
        }
        return this.getRoles().stream()
                .anyMatch(role->role.getName().equals(roleName));
    }
    public boolean isAdmin(){
        return hasRole("ROLE_ADMIN");
    }
    public boolean isManager() {
        return hasRole("ROLE_MANAGER");
    }
    public boolean isStudent() {
        return hasRole("ROLE_STUDENT");
    }

}
