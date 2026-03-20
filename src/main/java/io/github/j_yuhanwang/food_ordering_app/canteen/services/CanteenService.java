package io.github.j_yuhanwang.food_ordering_app.canteen.services;

import io.github.j_yuhanwang.food_ordering_app.canteen.dtos.CanteenDTO;
import io.github.j_yuhanwang.food_ordering_app.canteen.dtos.CanteenScheduleDTO;
import io.github.j_yuhanwang.food_ordering_app.canteen.dtos.HolidayScheduleDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * @author YuhanWang
 * @Date 19/03/2026 2:23 pm
 */

public interface CanteenService {
    //1. for all users
    CanteenDTO getCanteenById(Long canteenId);

    List<CanteenDTO> getAllCanteens();

    //2.for admin/manager - add, update, deactivate
    CanteenDTO addCanteen(CanteenDTO canteenDTO);

    //Allow Admin and Manager to make calls (to modify descriptions, name and description).
    CanteenDTO updateCanteenById(Long canteenId, CanteenDTO canteenDTO);

    CanteenDTO uploadCanteenImage(Long canteenId, MultipartFile file);

    void deactivateCanteen(Long canteenId);

    //Admin users only (for personnel transfers).
    void assignManager(Long canteenId, Long userId);


    //3.for schedules modification
    //For special holiday schedules, add and remove respectively
    HolidayScheduleDTO addHolidaySchedule(Long canteenId, HolidayScheduleDTO holidayDTO);

    void removeHolidaySchedule(Long canteenId,Long HolidayId);

    //For canteen regular schedules, update overall plan.
    List<CanteenScheduleDTO> updateWeeklySchedules(Long canteenId, List<CanteenScheduleDTO> scheduleDTOs);
}
