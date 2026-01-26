package io.github.j_yuhanwang.food_ordering_app.cart.entity;

import io.github.j_yuhanwang.food_ordering_app.auth_users.entity.User;
import jakarta.persistence.*;
import lombok.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a user's active shopping basket.
 * <p>
 * A Cart acts as a temporary holding area for {@link CartItem}s before they are
 * converted into an Order. Each user has exactly one active cart.
 *
 * @author BlairWang
 * @version 1.0
 * @date 24/01/2026 7:26 pm
 */

@Builder
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "carts")
@Data
public class Cart {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The user who owns this cart.
     * <p>
     * <b>Relationship:</b> One-to-One.
     * <br>
     * <b>Constraint:</b> A user can only have one active cart at a time (unique = true).
     */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)//Foreign key name
    @ToString.Exclude
    private User user;

    /**
     * The list of items currently selected by the user.
     * <p>
     * <b>CascadeType.ALL:</b> Saving the Cart automatically saves all added items.
     * <br>
     * <b>orphanRemoval = true:</b> If an item is removed from this list (e.g., user deletes an item),
     * it is strictly deleted from the database.
     */
    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    @ToString.Exclude
    List<CartItem> cartItems = new ArrayList<>();
}

