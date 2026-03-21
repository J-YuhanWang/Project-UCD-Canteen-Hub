package io.github.j_yuhanwang.food_ordering_app.canteen.controller;

import io.github.j_yuhanwang.food_ordering_app.canteen.dtos.CanteenDTO;
import io.github.j_yuhanwang.food_ordering_app.canteen.dtos.CanteenScheduleDTO;
import io.github.j_yuhanwang.food_ordering_app.canteen.dtos.HolidayScheduleDTO;
import io.github.j_yuhanwang.food_ordering_app.canteen.services.CanteenService;
import io.github.j_yuhanwang.food_ordering_app.response.Response;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * @author YuhanWang
 * @Date 21/03/2026 8:15 pm
 */
@RestController
@RequestMapping("/api/v1/canteens")
@RequiredArgsConstructor
@Slf4j
@Validated
public class CanteenController {
    private final CanteenService canteenService;

    //1. -----For all users-----
    //1.1 get Canteen By Id
    @GetMapping("/{id}")
    public Response<CanteenDTO> getCanteenById(@PathVariable Long id){
        log.info("API request to get canteen : {}", id);
        return Response.ok(canteenService.getCanteenById(id));
    }

    //1.2 get All Canteens
    @GetMapping
    public Response<List<CanteenDTO>> getAllCanteens(){
        log.info("API request to get all canteens");
        return Response.ok(canteenService.getAllCanteens());
    }

    //-----2.for admin/manager - add, update, deactivate-----
    //2.1 add Canteen
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public Response<CanteenDTO> addCanteen(@Valid @RequestBody CanteenDTO canteenDTO){
        log.info("API request to add new canteen : {}", canteenDTO.getName());
        return Response.ok(canteenService.addCanteen(canteenDTO));
    }

    //2.2 update Canteen By Id
    @PutMapping("/{canteenId}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public Response<CanteenDTO> updateCanteenById(
            @PathVariable Long canteenId,
            @RequestBody CanteenDTO canteenDTO){
        log.info("API request to update canteen info for ID: {}", canteenId);
        return Response.ok(canteenService.updateCanteenById(canteenId,canteenDTO));
    }

    //2.3 upload Canteen Image
    @PostMapping("/{canteenId}/image")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public Response<CanteenDTO> uploadCanteenImage(
            @PathVariable Long canteenId,
            @RequestParam("file") MultipartFile file){
        log.info("API request to upload image for canteen ID: {}", canteenId);
        return Response.ok(canteenService.uploadCanteenImage(canteenId,file));
    }

    // 2.4 deactivate Canteen
    @DeleteMapping("/{canteenId}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public Response<String> deactivateCanteen(@PathVariable Long canteenId){
        log.info("API request to deactivate canteen ID: {}", canteenId);
        canteenService.deactivateCanteen(canteenId);
        return Response.ok("Canteen has been deactivated successfully.");
    }

    //2.5 assign manager
    //Admin users only (for personnel transfers).
    @PutMapping("/{canteenId}/manager/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public Response<String> assignManager(
            @PathVariable("canteenId") Long canteenId,
            @PathVariable("userId") Long userId){
        log.info("API request to assign user {} as manager for canteen {}", userId, canteenId);
        canteenService.assignManager(canteenId,userId);
        return Response.ok("Manager assigned successfully to the canteen.");
    }

    //------3.for schedules modification------
    //3.1 add Holiday Schedule
    @PostMapping("/{canteenId}/holidays")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public Response<HolidayScheduleDTO> addHolidaySchedule(
            @PathVariable Long canteenId,
            @RequestBody @Valid HolidayScheduleDTO holidayDTO){
        log.info("API request to add holiday for canteen ID: {}", canteenId);
        return Response.ok(canteenService.addHolidaySchedule(canteenId,holidayDTO));
    }

    //3.2 remove Holiday Schedule
    @DeleteMapping("/{canteenId}/holidays/{holidayId}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public Response<String> removeHolidaySchedule(
            @PathVariable Long canteenId,
            @PathVariable Long holidayId) {
        log.info("API request to remove holiday {} from canteen {}", holidayId, canteenId);
        canteenService.removeHolidaySchedule(canteenId, holidayId);
        return Response.ok("Holiday schedule removed successfully");
    }

    //3.3 update Weekly Schedules
    @PutMapping("/{canteenId}/schedules")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public Response<List<CanteenScheduleDTO>> updateWeeklySchedules(
            @PathVariable Long canteenId,
            @RequestBody @Valid List<CanteenScheduleDTO> scheduleDTOs){
        log.info("API request to sync weekly schedules for canteen ID: {}", canteenId);
        return Response.ok(canteenService.updateWeeklySchedules(canteenId,scheduleDTOs));
    }
}
