package io.github.j_yuhanwang.food_ordering_app.canteen.mapper;

import io.github.j_yuhanwang.food_ordering_app.canteen.dtos.HolidayScheduleDTO;
import io.github.j_yuhanwang.food_ordering_app.canteen.entity.HolidaySchedule;
import org.springframework.stereotype.Component;

/**
 * @author YuhanWang
 * @Date 19/03/2026 11:58 am
 */
@Component
public class HolidayScheduleMapper {
    public HolidayScheduleDTO toDTO(HolidaySchedule entity){
        if(entity==null){
            return null;
        }
        return HolidayScheduleDTO.builder()
                .id(entity.getId())
                .specificDate(entity.getSpecificDate())
                .openingTime(entity.getOpeningTime())
                .closingTime(entity.getClosingTime())
                .description(entity.getDescription())
                .isClosed(entity.isClosed())
                .build();
    }

    public HolidaySchedule toEntity(HolidayScheduleDTO dto){
        if (dto == null) return null;

        return HolidaySchedule.builder()
                .id(dto.getId())
                .specificDate(dto.getSpecificDate())
                .openingTime(dto.getOpeningTime())
                .closingTime(dto.getClosingTime())
                .isClosed(dto.isClosed())
                .description(dto.getDescription())
                .build();
    }
}
