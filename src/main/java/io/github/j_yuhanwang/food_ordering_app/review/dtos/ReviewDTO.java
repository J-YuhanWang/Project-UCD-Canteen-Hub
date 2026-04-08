package io.github.j_yuhanwang.food_ordering_app.review.dtos;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Data Transfer Object for Customer Reviews.
 * Handles both input (creating a review) and output (displaying reviews).
 *
 * @author YuhanWang
 * @Date 02/02/2026 7:58 pm
 */

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ReviewDTO {
    private Long id;

    //User Info
    private Long userId;
    private String userName;
    private String userAvatarUrl;


    //Flatted Dish Info
    private Long dishId;
    private String dishName;
    private String dishImageUrl;

    @NotNull(message = "Rating is required")
    @Min(value = 1, message = "Rating must be at least 1")
    @Max(value = 5, message = "Rating cannot exceed 5")
    private Integer rating;

    @NotBlank(message = "Comment cannot be empty")
    private String comment;

    //Verification
    private Long orderId;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
}
