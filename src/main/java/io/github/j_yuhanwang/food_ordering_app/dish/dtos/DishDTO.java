package io.github.j_yuhanwang.food_ordering_app.dish.dtos;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalTime;

/**
 * @author YuhanWang
 * @Date 02/02/2026 4:28 pm
 */

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DishDTO {
    private Long id;

    @NotBlank(message = "Dish name is required")
    private String name;

    private String description;

    /**
     * Use BigDecimal for money to avoid precision loss.
     * @Positive ensures price is greater than 0.
     */
    @NotNull(message = "Price is required")
    @Positive(message = "Price must be greater than 0")
    private BigDecimal price;

    private String imageUrl;

    private String foodCategory; //"Drinks","Mains"

    //Availability Logic
    private boolean isAvailable;

    @JsonFormat(pattern = "HH:mm")
    private LocalTime availableStartTime;

    @JsonFormat(pattern = "HH:mm")
    private LocalTime availableEndTime;

    //Key Relationships
    // Reference ID for database relationships and routing.
    @NotNull(message = "Canteen id is required")
    private Long canteenId;

    // Redundant name to avoid extra API calls for UI display.
    private String canteenName;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private MultipartFile imageFile;

    private Double averageRating;

    private Integer reviewCount;

}
