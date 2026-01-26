package io.github.j_yuhanwang.food_ordering_app.canteen.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.github.j_yuhanwang.food_ordering_app.auth_users.entity.User;
import io.github.j_yuhanwang.food_ordering_app.menu.entity.Menu;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents UCD Canteen.
 * <p>
 * This entity acts as the aggregate root for all food-related operations.
 * It manages its own lifecycle along with its Menus and Operating Schedules(daily and holiday schedules).
 * If a Canteen is deleted, all associated Menus and Schedules are strictly deleted.
 *
 * @author BlairWang
 * @version 1.0
 * @date 24/01/2026 7:39 pm
 */

@NoArgsConstructor
@AllArgsConstructor
@Entity
@Builder
@Data
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

    /**
     * The list of menus offered by this canteen.
     * <p>
     * <b>Orphan Removal:</b> true (Removing a menu from this list deletes it from the database)
     */
    @OneToMany(mappedBy = "canteen", cascade = CascadeType.ALL)
    @Builder.Default
    @ToString.Exclude
    private List<Menu> menus = new ArrayList<>();

    /**
     * The specific user account designated as the manager of this canteen.
     * This user has permissions to update menus and schedules.
     */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "manager_id", referencedColumnName = "id", unique = true)
    @JsonIgnore
    @ToString.Exclude
    private User manager;

//    @Builder.Default
//    private boolean isDeleted=false;

    /**
     * Standard weekly operating schedules (e.g., Mon-Fri 9am-5pm).
     */
    @OneToMany(mappedBy = "canteen", cascade = CascadeType.ALL)
    @Builder.Default //Without this annotation, Lombok will neglect the initialized value
    @ToString.Exclude
    private List<CanteenSchedule> canteenSchedules = new ArrayList<>();

    /**
     * Special schedules for specific dates (e.g., Bank Holidays, Christmas),
     * which override the standard weekly schedule.
     */
    @OneToMany(mappedBy = "canteen", cascade = CascadeType.ALL)
    @Builder.Default
    @ToString.Exclude
    private List<HolidaySchedule> holidaySchedules = new ArrayList<>();
}
