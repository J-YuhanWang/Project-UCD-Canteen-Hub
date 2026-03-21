package io.github.j_yuhanwang.food_ordering_app.canteen.services;

import io.github.j_yuhanwang.food_ordering_app.auth_users.entity.User;
import io.github.j_yuhanwang.food_ordering_app.auth_users.repository.UserRepository;
import io.github.j_yuhanwang.food_ordering_app.aws.services.AwsS3Service;
import io.github.j_yuhanwang.food_ordering_app.canteen.dtos.CanteenDTO;
import io.github.j_yuhanwang.food_ordering_app.canteen.dtos.CanteenScheduleDTO;
import io.github.j_yuhanwang.food_ordering_app.canteen.dtos.HolidayScheduleDTO;
import io.github.j_yuhanwang.food_ordering_app.canteen.entity.Canteen;
import io.github.j_yuhanwang.food_ordering_app.canteen.entity.CanteenSchedule;
import io.github.j_yuhanwang.food_ordering_app.canteen.entity.HolidaySchedule;
import io.github.j_yuhanwang.food_ordering_app.canteen.mapper.CanteenMapper;
import io.github.j_yuhanwang.food_ordering_app.canteen.mapper.CanteenScheduleMapper;
import io.github.j_yuhanwang.food_ordering_app.canteen.mapper.HolidayScheduleMapper;
import io.github.j_yuhanwang.food_ordering_app.canteen.repository.CanteenRepository;
import io.github.j_yuhanwang.food_ordering_app.canteen.repository.HolidayScheduleRepository;
import io.github.j_yuhanwang.food_ordering_app.exceptions.BadRequestException;
import io.github.j_yuhanwang.food_ordering_app.exceptions.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.time.DayOfWeek;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author YuhanWang
 * @Date 19/03/2026 2:39 pm
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class CanteenServiceImpl implements CanteenService{
    private final CanteenRepository canteenRepository;
    private final CanteenMapper canteenMapper;
    private final UserRepository userRepository;
    private final CanteenScheduleMapper canteenScheduleMapper;
    private final HolidayScheduleMapper holidayScheduleMapper;
    private final AwsS3Service awsS3Service;
    private final HolidayScheduleRepository holidayScheduleRepository;

    //1. -----for all users-----
    @Override
    public CanteenDTO getCanteenById(Long canteenId) {
        log.info("Inside getCanteenById");
        Canteen canteen = canteenRepository.findByIdAndIsDeletedFalse(canteenId).orElseThrow(
                ()->new ResourceNotFoundException("Canteen","id",canteenId)
        );
        return canteenMapper.toDTO(canteen);
    }

    @Override
    public List<CanteenDTO> getAllCanteens() {
        log.info("Inside getAllCanteens");
        List<Canteen> canteenList = canteenRepository.findAllByIsDeletedFalse();
        return canteenList.stream()
                .map(canteenMapper::toDTO)
                .toList();
    }

    //-----2.for admin/manager - add, update, deactivate-----
    @Override
    @Transactional
    public CanteenDTO addCanteen(CanteenDTO canteenDTO) {
        log.info("Inside addCanteen");
        //if exists, throw the exception
        if(canteenRepository.existsByNameIgnoreCase(canteenDTO.getName())){
            log.warn("Canteen name [{}] already exists", canteenDTO.getName());
            throw new BadRequestException("Canteen name already exists!");
        }
        Canteen canteen = canteenMapper.toEntity(canteenDTO);
        Canteen savedCanteen = canteenRepository.save(canteen);
        return canteenMapper.toDTO(savedCanteen);
    }

    @Override
    @Transactional
    public CanteenDTO updateCanteenById(Long canteenId, CanteenDTO canteenDTO) {
        log.info("Attempting to update basic info for canteen by id: {}", canteenId);
        //Locate the canteen entity by ID, update canteenDTO to an entity partially,  store it again in the database, and return the DTO.
        Canteen existingCanteen = canteenRepository.findByIdAndIsDeletedFalse(canteenId).orElseThrow(
                ()->new ResourceNotFoundException("Canteen","id",canteenId)
        );
        //Handling "partial updates" and "self-conflicts" in naming.
        if(canteenDTO.getName()!=null && !canteenDTO.getName().trim().isEmpty()){
            //Key criterion: Duplicate checking is only performed when a newly submitted name differs from an existing name in the database.
            if(!existingCanteen.getName().equalsIgnoreCase(canteenDTO.getName().trim())){
                //if new name exists in database, throw exception, otherwise set the new name
                if(canteenRepository.existsByNameIgnoreCase(canteenDTO.getName().trim())){
                    log.warn("Update failed: New name [{}] is already taken by another canteen", canteenDTO.getName());
                    throw new BadRequestException("Canteen name already exists!");
                }

                existingCanteen.setName(canteenDTO.getName().trim());
            }
        }

        if(canteenDTO.getCanteenType()!=null){
            existingCanteen.setCanteenType(canteenDTO.getCanteenType());
        }
        if(canteenDTO.getDescription()!=null){
            existingCanteen.setDescription(canteenDTO.getDescription());
        }
        log.info("Successfully updated basic info for canteen ID: {}", canteenId);
        Canteen savedCanteen= canteenRepository.save(existingCanteen);
        return canteenMapper.toDTO(savedCanteen);
    }

    @Override
    @Transactional
    public CanteenDTO uploadCanteenImage(Long canteenId, MultipartFile file) {
        log.info("Attempting to upload image for canteen id: {}", canteenId);
        //1. fetch the canteen entity
        Canteen canteen = canteenRepository.findByIdAndIsDeletedFalse(canteenId).orElseThrow(
                ()->new ResourceNotFoundException("Canteen","id",canteenId)
        );
        //2. Delete the old canteen image in cloud if it exists at first, but not throw the exception
        if(StringUtils.hasText(canteen.getImageUrl())){
            try{
                String oldUrl = canteen.getImageUrl();
                //substring(index + 1): Retrieves the filename after "/".
                String oldKey = "canteen/"+oldUrl.substring(oldUrl.lastIndexOf("/")+1);
                awsS3Service.deleteFile(oldKey);
                log.info("Deleted old canteen image from S3");
            }catch(Exception e){
                log.error("Failed to delete old canteen image from S3, proceeding with upload: {}", e.getMessage());
            }
        }

        //3.Upload new image
        //file.getOriginalFileName() is the original file name uploaded by customer to the system
        String filename = UUID.randomUUID().toString()+"_"+file.getOriginalFilename();
        String keyName = "canteen/"+filename;
        String newImgUrl = awsS3Service.uploadFile(keyName,file);

        //4.update the url data to repository
        canteen.setImageUrl(newImgUrl);
        Canteen savedCanteen = canteenRepository.save(canteen);
        log.info("New canteen image uploaded and database updated: {}", newImgUrl);

        return canteenMapper.toDTO(savedCanteen);
    }

    @Override
    @Transactional
    public void deactivateCanteen(Long canteenId) {
        log.info("Attempting to deactivate canteen ID: {}", canteenId);
        Canteen canteen = canteenRepository.findByIdAndIsDeletedFalse(canteenId).orElseThrow(
                ()->new ResourceNotFoundException("Canteen","id",canteenId)
        );

        canteen.setDeleted(true);
        //Core Operation1: Add a timestamp suffix to the name to release the original name
        //old name: "Pi Restaurant" -> Current name: "Pi Restaurant_DELETED_1710923088"
        canteen.setName(canteen.getName()+"_DELETED_"+System.currentTimeMillis());
        //Core operation2: release the manager
        canteen.setManager(null);
        canteenRepository.save(canteen);

        log.info("Canteen {} has been soft-deleted and renamed. Name and manager released.", canteenId);
    }

    //Admin users only (for personnel transfers).
    @Override
    @Transactional
    public void assignManager(Long canteenId, Long userId) {
        log.info("Attempting to assign manager {} to canteen {}",userId, canteenId);
        //1. verify if the canteen exists
        Canteen canteen = canteenRepository.findByIdAndIsDeletedFalse(canteenId).orElseThrow(
                ()->new ResourceNotFoundException("Canteen","id",canteenId)
        );
        //2. verify if the manager exists
        User manager = userRepository.findById(userId).orElseThrow(
                ()->new ResourceNotFoundException("User","id",userId)
        );

        //3.Core business logic validation: Is the user already a manager at other restaurant?
        canteenRepository.findByManagerAndDeletedIsFalse(manager).ifPresent(otherCanteen->{
            if(!otherCanteen.getId().equals(canteenId)){
                log.warn("Assign manager failed: User [{}] is already managing Canteen [{}]",
                        manager.getName(), otherCanteen.getName());
                throw new BadRequestException("This user is already a manager of another canteen!");
            }
        });
        //4. Binding: Canteen is the maintainer of the relationship (owns @JoinColumn)
        canteen.setManager(manager);
        //5.update Canteen
        Canteen savedCanteen = canteenRepository.save(canteen);

        log.info("Successfully assigned User [{}] as manager for Canteen [{}]",
                manager.getName(), canteen.getName());

    }

    //------3.for schedules modification------
    @Override
    @Transactional
    public HolidayScheduleDTO addHolidaySchedule(Long canteenId, HolidayScheduleDTO holidayDTO) {
        log.info("Attempting to add holiday schedule for Canteen ID: {}", canteenId);
        //1.verify if the canteen exists
        Canteen canteen = canteenRepository.findByIdAndIsDeletedFalse(canteenId).orElseThrow(
                ()->new ResourceNotFoundException("Canteen","id",canteenId)
        );

        //2.Duplicate checking: Preventing the same restaurant from adding two holidays on the same day (defensive programming)
        boolean alreadyExist = canteen.getHolidaySchedules().stream()
                .anyMatch(h->h.getSpecificDate().equals(holidayDTO.getSpecificDate()));
        if(alreadyExist){
            log.warn("Holiday schedule for date [{}] already exists in Canteen [{}]",
                    holidayDTO.getSpecificDate(), canteen.getName());
            throw new BadRequestException("A holiday schedule for this date already exists!");
        }
        //3. if not exist, then holidaySchedule add canteen
        HolidaySchedule holiday = holidayScheduleMapper.toEntity(holidayDTO);
        //Because `@ManyToOne(name="canteen_id")` in the `HolidaySchedule` entity is the relationship maintainer.
        holiday.setCanteen(canteen);

        //4. save and return
        HolidaySchedule savedHoliday = holidayScheduleRepository.save(holiday);
        log.info("Successfully added holiday schedule for date [{}] to Canteen [{}]",
                savedHoliday.getSpecificDate(), canteen.getName());

        return holidayScheduleMapper.toDTO(savedHoliday);
    }

    @Override
    @Transactional
    public void removeHolidaySchedule(Long canteenId, Long holidayId) {
        log.info("Attempting to remove holiday id [{}] from Canteen ID [{}]", holidayId, canteenId);
        Canteen canteen = canteenRepository.findByIdAndIsDeletedFalse(canteenId).orElseThrow(
                ()->new ResourceNotFoundException("Canteen","id",canteenId)
        );
        HolidaySchedule holiday = holidayScheduleRepository.findById(holidayId).orElseThrow(
                ()->new ResourceNotFoundException("Holiday","id",holidayId)
        );
        // Core Defense: Preventing Horizontal Privilege Escalation
        // Suppose a hacker knows that restaurant A's holidayId is 45, and maliciously calls /canteens/999(restaurant B)/holidays/45
        // in an attempt to delete other people's data. This verification will block this type of attack
        if(!holiday.getCanteen().getId().equals(canteenId)){
            log.error("Security Alert: Attempt to delete holiday [{}] that does not belong to Canteen [{}]",
                    holidayId, canteenId);
            throw new BadRequestException("This holiday schedule does not belong to the specified canteen.");
        }

        holidayScheduleRepository.delete(holiday);
        log.info("Successfully deleted holiday schedule ID [{}] for Canteen [{}]", holidayId, canteen.getName());
    }

    @Override
    @Transactional
    public List<CanteenScheduleDTO> updateWeeklySchedules(Long canteenId, List<CanteenScheduleDTO> scheduleDTOs) {
        log.info("Attempting to synchronize weekly schedules for Canteen ID: {}", canteenId);
        // 1.fetch the canteen
        Canteen canteen = canteenRepository.findByIdAndIsDeletedFalse(canteenId).orElseThrow(
                ()->new ResourceNotFoundException("Canteen","id",canteenId)
        );

        // 2.Convert the existing work schedule in the database into a Map, using the day of the week (DayOfWeek) as the key.
        //The advantage is that the complexity of subsequently searching for the existence of a specific day drops dramatically from O(N) to O(1), resulting in extremely fast lookups.
        Map<DayOfWeek, CanteenSchedule> existingScheduleMap = canteen.getCanteenSchedules().stream()
                .collect(Collectors.toMap(CanteenSchedule::getDayOfWeek,schedule->schedule));

        //Record exactly which days the frontend sent (to identify which days were subsequently deleted by the frontend).
        Set<DayOfWeek> incomingDays = new HashSet<>();

        //3.Core comparison: Iterates through the DTO list sent from the front end
        for(CanteenScheduleDTO dto: scheduleDTOs){
            incomingDays.add(dto.getDayOfWeek());

            //3.1 if the day of week already exists in the database: update the value
            if(existingScheduleMap.containsKey(dto.getDayOfWeek())){
                //update new schedule(opening time,closing time, isClosed) to old schedule
                CanteenSchedule existingSchedule = existingScheduleMap.get(dto.getDayOfWeek());
                //JPA(Java Persistence API) Managed State and Dirty checking, can set entity's attribute from dto fields directly
                existingSchedule.setOpeningTime(dto.getOpeningTime());
                existingSchedule.setClosingTime(dto.getClosingTime());
                existingSchedule.setClosed(dto.isClosed());

            }else {
                //3.2 if the day of week does not exist in the database: insert
                CanteenSchedule newSchedule = canteenScheduleMapper.toEntity(dto);
                newSchedule.setCanteen(canteen);
                canteen.getCanteenSchedules().add(newSchedule);
            }
        }

        // 4.If the old schedule's DayOfWeek is not in incomingDays, remove it from the list.
        //Due to orphanRemoval = true, the removed entity will be automatically converted into a DELETE statement by JPA.
        canteen.getCanteenSchedules().removeIf(
                existingSchedule-> !incomingDays.contains(existingSchedule.getDayOfWeek())
        );

        //5. Save (because cascade = CascadeType.ALL is configured, saving Canteen will automatically trigger
        // all the above add, delete, and modify operations)
        Canteen savedCanteen = canteenRepository.save(canteen);
        log.info("Successfully synchronized weekly schedules for Canteen ID: {}", canteenId);

        //6.Convert the saved (even with newly generated IDs) latest schedule list into a DTO list and return it to the front end.
        return savedCanteen.getCanteenSchedules().stream()
                .map(canteenScheduleMapper::toDTO) //(java8: className::functionName)
                .toList();
    }
}
