package io.github.j_yuhanwang.food_ordering_app.canteen.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.DayOfWeek;
import java.time.LocalTime;

/**
 * Defines the standard recurring operating hours for a specific day of the week.
 * <p>
 * Example: "Open every MONDAY from 09:00 to 17:00".
 * This schedule applies to every week unless overridden by a {@link HolidaySchedule}.
 *
 * @author BlairWang
 * @version 1.0
 * @date 24/01/2026 9:05 pm
 */

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
@Entity
// Ensures a canteen cannot have duplicate schedules for the same day (e.g., two entries for MONDAY)
@Table(name = "canteen_schedules", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"canteen_id", "day_of_week"})
})
public class CanteenSchedule {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "canteen_id", nullable = false)
    @ToString.Exclude
    private Canteen canteen;

    /**
     * Represents the day of the week (MONDAY, TUESDAY, etc.).
     * Stored as a String in the database for readability.
     */
    @Enumerated(EnumType.STRING)
    private DayOfWeek dayOfWeek; // Java ENUMS：MONDAY, TUESDAY...

    private LocalTime openingTime;

    private LocalTime closingTime;

    /**
     * Indicates if the canteen is closed for the entire day.
     * If true, openingTime and closingTime can be ignored or null.
     */
    @Builder.Default
    private boolean isClosed = false;
}
