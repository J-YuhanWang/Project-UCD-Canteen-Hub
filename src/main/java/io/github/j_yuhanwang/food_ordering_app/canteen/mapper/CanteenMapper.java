package io.github.j_yuhanwang.food_ordering_app.canteen.mapper;

import io.github.j_yuhanwang.food_ordering_app.canteen.dtos.CanteenDTO;
import io.github.j_yuhanwang.food_ordering_app.canteen.dtos.CanteenScheduleDTO;
import io.github.j_yuhanwang.food_ordering_app.canteen.dtos.HolidayScheduleDTO;
import io.github.j_yuhanwang.food_ordering_app.canteen.entity.Canteen;
import io.github.j_yuhanwang.food_ordering_app.canteen.entity.CanteenSchedule;
import io.github.j_yuhanwang.food_ordering_app.canteen.entity.HolidaySchedule;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * @author YuhanWang
 * @Date 19/03/2026 12:01 pm
 */
@Component
@RequiredArgsConstructor
public class CanteenMapper {
    private final CanteenScheduleMapper canteenScheduleMapper;
    private final HolidayScheduleMapper holidayScheduleMapper;

    //Entity -> DTO
    public CanteenDTO toDTO(Canteen entity){
        if(entity == null){
            return null;
        }
        List<CanteenScheduleDTO> scheduleDTOs = new ArrayList<>();
        if(entity.getCanteenSchedules()!=null){
            scheduleDTOs = entity.getCanteenSchedules().stream()
                    .map(canteenScheduleMapper::toDTO)
                    .toList();
        }

        List<HolidayScheduleDTO> holidayDTOs = new ArrayList<>();
        if(entity.getHolidaySchedules()!=null){
            holidayDTOs = entity.getHolidaySchedules().stream()
                    .map(holidayScheduleMapper::toDTO)
                    .toList();
        }

        return CanteenDTO.builder()
                .id(entity.getId())
                .name(entity.getName())
                .canteenType(entity.getCanteenType())
                .description(entity.getDescription())
                .imageUrl(entity.getImageUrl())
                .schedules(scheduleDTOs)
                .holidays(holidayDTOs)
                // ignore: isOpen, todayOpeningTime, todayClosingTime (calculate by Service)
                // ignore: manager (frontend does not need this)
                // ignore: isDeleted (not contained in DTO)
                .build();
    }

    //DTO -> Entity
    public Canteen toEntity(CanteenDTO dto){
        if(dto==null){
            return null;
        }
        Canteen canteen = Canteen.builder()
                .id(dto.getId())
                .name(dto.getName())
                .canteenType(dto.getCanteenType())
                .description(dto.getDescription())
                .imageUrl(dto.getImageUrl())
                //isDeleted will be automatically set to false by @Builder.Default
                //manager is currently null, it can be manually set using `setManager()` in the Service layer.
                .build();

        if(dto.getSchedules()!=null){
            List<CanteenSchedule> schedules = dto.getSchedules().stream()
                    .map(canteenScheduleDTO -> {
                        CanteenSchedule schedule = canteenScheduleMapper.toEntity(canteenScheduleDTO);
                        schedule.setCanteen(canteen);
                        return schedule;
                    })
                    .toList();
            canteen.setCanteenSchedules(schedules);
        }

        if(dto.getHolidays()!=null){
            List<HolidaySchedule> holidays = dto.getHolidays().stream()
                    .map(holidayScheduleDTO -> {
                        HolidaySchedule holiday = holidayScheduleMapper.toEntity(holidayScheduleDTO);
                        holiday.setCanteen(canteen);
                        return holiday;
                    })
                    .toList();
            canteen.setHolidaySchedules(holidays);
        }

        return canteen;
    }

}
