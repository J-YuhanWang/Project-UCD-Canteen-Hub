package io.github.j_yuhanwang.food_ordering_app.canteen.mapper;

import io.github.j_yuhanwang.food_ordering_app.canteen.dtos.CanteenScheduleDTO;
import io.github.j_yuhanwang.food_ordering_app.canteen.entity.CanteenSchedule;
import org.springframework.stereotype.Component;

/**
 * @author YuhanWang
 * @Date 19/03/2026 11:48 am
 */
@Component
public class CanteenScheduleMapper {
    public CanteenScheduleDTO toDTO(CanteenSchedule entity){
        if(entity==null){
            return null;
        }
        return CanteenScheduleDTO.builder()
                .id(entity.getId())
                .dayOfWeek(entity.getDayOfWeek())
                .openingTime(entity.getOpeningTime())
                .closingTime(entity.getClosingTime())
                .isClosed(entity.isClosed())
                .build();
    }

    public CanteenSchedule toEntity(CanteenScheduleDTO dto){
        if(dto==null){
            return null;
        }
        return CanteenSchedule.builder()
                .id(dto.getId())
                .dayOfWeek(dto.getDayOfWeek())
                .openingTime(dto.getOpeningTime())
                .closingTime(dto.getClosingTime())
                .isClosed(dto.isClosed())
                .build();
    }
}
