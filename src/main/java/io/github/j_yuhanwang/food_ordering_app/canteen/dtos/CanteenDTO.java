package io.github.j_yuhanwang.food_ordering_app.canteen.dtos;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.github.j_yuhanwang.food_ordering_app.canteen.entity.CanteenSchedule;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;
import java.util.List;

/**
 * @author YuhanWang
 * @Date 02/02/2026 2:23 pm
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CanteenDTO {

    private Long id;

    @NotBlank(message = "Canteen name is required")
    private String name;

    private String canteenType;

    private String description;

    private String imageUrl;

    //calculated by service layer
    private boolean isOpen;

    @JsonFormat(pattern = "HH:mm")
    private LocalTime todayOpeningTime;

    @JsonFormat(pattern = "HH:mm")
    private LocalTime todayClosingTime;

    private List<CanteenScheduleDTO> schedules;

    private List<HolidayScheduleDTO> holidays;

    //List<DishDTO> dishes and User manager not shown in DTO
}
