package io.github.j_yuhanwang.food_ordering_app.cart.entity;

import io.github.j_yuhanwang.food_ordering_app.dish.entity.Dish;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import lombok.*;

import java.math.BigDecimal;

/**
 * Represents a specific dish item added to the shopping cart.
 * <p>
 * This entity links a {@link Cart} with a specific {@link Dish} item.
 * It tracks the quantity and calculated price for that specific selection.
 *
 * @author BlairWang
 * @version 1.0
 * @date 26/01/2026 8:39 am
 */

@Builder
@Entity
@NoArgsConstructor
@AllArgsConstructor
// Ensures that a specific dish appears only once in a single cart.
// If the user adds the same dish again, the application should update the quantity, not create a new row.
@Table(
        name = "cart_items",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"cart_id", "dish_id"})
        }
)
@Data
public class CartItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cart_id", nullable = false)
    @ToString.Exclude
    private Cart cart;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dish_id", nullable = false)
    @ToString.Exclude
    private Dish dish;

    /**
     * The number of this specific dish ordered.
     * Must be at least 1.
     */
    @Column(nullable = false)
    @Min(value = 1, message = "Quantity must be at least 1")
    private int quantity;

    /**
     * Snapshot of the unit price at the time of adding to cart.
     * Stored to calculate subtotal accurately.
     */
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal pricePerUnit;

    /**
     * Calculated field: quantity * pricePerUnit.
     */
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal subtotal;

}
