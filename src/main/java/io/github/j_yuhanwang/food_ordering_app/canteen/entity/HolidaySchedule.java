package io.github.j_yuhanwang.food_ordering_app.canteen.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Defines special operating hours for specific calendar dates.
 * <p>
 * These records represent exceptions to the standard {@link CanteenSchedule}.
 * They are used for public holidays, university breaks, or special events.
 * <p>
 * Logic: If a HolidaySchedule exists for a specific date (e.g., 2026-03-17),
 * the system should prioritize this record over the standard DayOfWeek schedule.
 *
 * @author BlairWang
 * @version 1.0
 * @date 24/01/2026 9:39 pm
 */

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Setter
@Getter
@Table(name = "holiday_schedules")
public class HolidaySchedule {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "canteen_id", nullable = false)
    private Canteen canteen;

    /**
     * The specific calendar date for this exception rule.
     * Example: 2026-03-17 (St. Patrick's Day).
     */
    @Column(nullable = false)
    private LocalDate specificDate;

    private LocalTime openingTime;// Opening time of specific date

    private LocalTime closingTime;// Closing time of specific date

    @Column(nullable = false)
    @Builder.Default
    private boolean isClosed = false;

    /**
     * Reason for the special schedule (e.g., "Bank Holiday", "UCD Winter Break").
     */
    private String description;

}
