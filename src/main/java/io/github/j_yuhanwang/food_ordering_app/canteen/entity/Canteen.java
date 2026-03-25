package io.github.j_yuhanwang.food_ordering_app.canteen.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.github.j_yuhanwang.food_ordering_app.auth_users.entity.User;
import io.github.j_yuhanwang.food_ordering_app.dish.entity.Dish;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents UCD Canteen.
 * <p>
 * This entity acts as the aggregate root for all food-related operations.
 * It manages its own lifecycle along with its Dishes and Operating Schedules(daily and holiday schedules).
 * If a Canteen is deleted, all associated Dishes and Schedules are strictly deleted.
 *
 * @author BlairWang
 * @version 1.0
 * @date 24/01/2026 7:39 pm
 */

@NoArgsConstructor
@AllArgsConstructor
@Entity
@Builder
@Getter
@Setter
@Table(name = "canteens")
public class Canteen {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The unique name of the canteen (e.g., "Main Restaurant", "Pi Restaurant").
     * Must be unique across the system to avoid confusion.
     */
    @Column(nullable = false, unique = true)
    @NotBlank(message = "Canteen name is required")
    private String name;

    private String canteenType;

    @Column(length = 500)
    private String description;

    private String imageUrl;

    /**
     * The list of dishes offered by this canteen.
     * <p>
     * <b>Orphan Removal:</b> true (Removing a dish from this list deletes it from the database)
     */
    @OneToMany(mappedBy = "canteen", cascade = CascadeType.ALL)
    @Builder.Default
    private List<Dish> dishes = new ArrayList<>();

    /**
     * The specific user account designated as the manager of this canteen.
     * This user has permissions to update dishes and schedules.
     */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "manager_id", referencedColumnName = "id", unique = true)
    @JsonIgnore
    private User manager;

    @Builder.Default
    @Column(name = "is_deleted", nullable = false)
    private boolean isDeleted=false;

    /**
     * Standard weekly operating schedules (e.g., Mon-Fri 9am-5pm).
     */
    @OneToMany(mappedBy = "canteen", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default //Without this annotation, Lombok will neglect the initialized value
    private List<CanteenSchedule> canteenSchedules = new ArrayList<>();

    /**
     * Special schedules for specific dates (e.g., Bank Holidays, Christmas),
     * which override the standard weekly schedule.
     */
    @OneToMany(mappedBy = "canteen", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<HolidaySchedule> holidaySchedules = new ArrayList<>();
}
