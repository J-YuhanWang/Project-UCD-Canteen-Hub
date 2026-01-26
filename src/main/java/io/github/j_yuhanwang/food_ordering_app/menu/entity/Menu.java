package io.github.j_yuhanwang.food_ordering_app.menu.entity;

import io.github.j_yuhanwang.food_ordering_app.canteen.entity.Canteen;
import io.github.j_yuhanwang.food_ordering_app.order.entity.OrderItem;
import io.github.j_yuhanwang.food_ordering_app.review.entity.Review;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a specific food item or dish available for purchase.
 * <p>
 * A Menu item belongs to a specific {@link Canteen}. It includes pricing details,
 * availability windows (e.g., Breakfast only), and links to customer reviews.
 *
 * @author BlairWang
 * @version 1.0
 * @date 24/01/2026 9:57 pm
 */

@Table(name="menus")
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Menu {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The name of the dish (e.g., "Kung Pao Chicken").
     */
    @Column(nullable = false)
    private String name;

    private String description;

    @Column(nullable = false,precision = 10,scale=2)
    private BigDecimal price;

    private String imageUrl;

    /**
     * The canteen that provides this menu item.
     * Relationship: Many Menu items belong to One Canteen.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="canteen_id", nullable = false)
    @ToString.Exclude
    private Canteen canteen;

    /**
     * Historical record of orders containing this item.
     * Used for sales analysis or popularity ranking.
     */
    @OneToMany(mappedBy = "menu",cascade = CascadeType.ALL)
    @ToString.Exclude
    @Builder.Default
    private List<OrderItem> orderItems = new ArrayList<>();

    /**
     * Customer reviews specific to this dish.
     */
    @OneToMany(mappedBy = "menu",cascade = CascadeType.ALL)
    @ToString.Exclude
    @Builder.Default
    private List<Review> reviews = new ArrayList<>();

    /**
     * Category tag (e.g., "Main Course", "Beverage", "Dessert").
     */
    private String foodCategory;
//
//    @Builder.Default
//    private boolean isDeleted=false;

    // -----------------------------------------------------------------
    // Availability Logic
    // -----------------------------------------------------------------

    /**
     * Start time for serving this item (e.g., 08:00 for breakfast items).
     * If null, the item is available all day (subject to Canteen opening hours).
     */
    private LocalTime availableStartTime;

    /**
     * End time for serving this item (e.g., 11:00 for breakfast items).
     */
    private LocalTime availableEndTime;

    /**
     * Utility method to check if the item is currently orderable based on the time of day.
     * <p>
     * Annotated with {@code @Transient} because this boolean is calculated in real-time
     * and not stored in the database.
     *
     * @return true if current time is within the start/end window, or if no window is defined.
     */
    @Transient
    public boolean isAvailableNow(){
        if(availableStartTime==null || availableEndTime==null){
            return true;
        }
        LocalTime now = LocalTime.now();
        // Returns true if now is NOT before start AND NOT after end (inclusive range)
        return !now.isBefore(availableStartTime) && !now.isAfter(availableEndTime);
    }

}