package io.github.j_yuhanwang.food_ordering_app.canteen.entity;/*
 * @author BlairWang
 * @Date 24/01/2026 9:05 pm
 * @Version 1.0
 */

import jakarta.persistence.*;
import lombok.*;

import java.time.DayOfWeek;
import java.time.LocalTime;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
@Entity
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

    @Enumerated(EnumType.STRING)
    private DayOfWeek dayOfWeek; // Java ENUMS：MONDAY, TUESDAY...

    private LocalTime openingTime;

    private LocalTime closingTime;

    @Builder.Default
    private boolean isClosed = false;
}
