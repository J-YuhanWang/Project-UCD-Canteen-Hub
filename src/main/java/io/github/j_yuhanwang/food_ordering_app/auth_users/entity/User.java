package io.github.j_yuhanwang.food_ordering_app.auth_users.entity;/*
 * @author BlairWang
 * @Date 22/01/2026 8:14 pm
 * @Version 1.0
 */

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.github.j_yuhanwang.food_ordering_app.enums.UserStatus;
import io.github.j_yuhanwang.food_ordering_app.order.entity.Order;
import io.github.j_yuhanwang.food_ordering_app.review.entity.Review;
import io.github.j_yuhanwang.food_ordering_app.role.entity.Role;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name="users")
@Builder
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)//auto-increment
    private Long id;

}
