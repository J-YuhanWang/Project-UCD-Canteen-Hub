package io.github.j_yuhanwang.food_ordering_app.canteen.entity;/*
 * @author BlairWang
 * @Date 24/01/2026 9:39 pm
 * @Version 1.0
 */

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
@Table(name = "holiday_schedules")
public class HolidaySchedule {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "canteen_id", nullable = false)
    @ToString.Exclude
    private Canteen canteen;

    @Column(nullable = false)
    private LocalDate specificDate;//Specific date, such as 2026-03-17 (St. Patrick's Day)

    private LocalTime openingTime;// Opening time of specific date

    private LocalTime closingTime;// Closing time of specific date

    @Column(nullable = false)
    @Builder.Default
    private boolean isClosed = false;

    private String description; //Bank Holiday or UCD Winter Break

}
