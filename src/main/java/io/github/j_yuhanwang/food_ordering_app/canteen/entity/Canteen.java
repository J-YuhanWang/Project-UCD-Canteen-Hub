package io.github.j_yuhanwang.food_ordering_app.canteen.entity;/*
 * @author BlairWang
 * @Date 24/01/2026 7:39 pm
 * @Version 1.0
 */

import io.github.j_yuhanwang.food_ordering_app.auth_users.entity.User;
import io.github.j_yuhanwang.food_ordering_app.menu.entity.Menu;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

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

    private String name;

    private String canteenType;

    private String description;

    //canteen is tightly coupled with the menu
    // if a canteen is deleted, the menu data associated with that canteen will also be deleted
    @OneToMany(mappedBy = "canteen", cascade = CascadeType.ALL)
    @Builder.Default
    @ToString.Exclude
    private List<Menu> menu = new ArrayList<>();

    @OneToOne
    @JoinColumn(name = "manager_id", referencedColumnName = "id", unique = true)
    private User manager;

    @Builder.Default
    private boolean isDeleted = false;

    //canteen is tightly coupled with the canteenSchedules
    @OneToMany(mappedBy = "canteen", cascade = CascadeType.ALL)
    @Builder.Default //Without this annotation, Lombok will neglect the initialized value
    @ToString.Exclude
    private List<CanteenSchedule> canteenSchedules = new ArrayList<>();

    @OneToMany(mappedBy = "canteen",cascade = CascadeType.ALL)
    @Builder.Default
    @ToString.Exclude
    private List<HolidaySchedule> holidaySchedules = new ArrayList<>();
}
