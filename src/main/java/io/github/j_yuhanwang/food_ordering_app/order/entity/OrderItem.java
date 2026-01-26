package io.github.j_yuhanwang.food_ordering_app.order.entity;

import io.github.j_yuhanwang.food_ordering_app.menu.entity.Menu;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import lombok.*;

import java.math.BigDecimal;

/**
 * Represents a specific line item within an Order (e.g., "2x Kung Pao Chicken").
 * <p>
 * <b>Key Concept: Price Snapshot</b>
 * <br>
 * This entity stores the price <i>at the moment of purchase</i>. Even if the
 * original {@link Menu} price changes later, this record remains unchanged
 * to ensure historical financial accuracy.
 *
 * @author BlairWang
 * @version 1.0
 * @date 26/01/2026 8:39 am
 */

@Builder
@Entity
@NoArgsConstructor
@AllArgsConstructor
// Ensures that a specific dish appears only once in a single order (merged quantity)
@Table(name = "order_items",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"order_id", "menu_id"})
        }
)
@Data
public class OrderItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    @ToString.Exclude
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "menu_id", nullable = false)
    @ToString.Exclude
    private Menu menu;

    /**
     * The number of items ordered.
     */
    @Column(nullable = false)
    @Min(value = 1, message = "Quantity must be at least 1")
    private int quantity;

    /**
     * <b>Price Snapshot:</b> The price of a single unit at the time of order placement.
     * This decouples the order history from future menu price changes.
     */
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal pricePerUnit;

    /**
     * Calculated field: quantity * pricePerUnit.
     */
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal subtotal;

}
