package io.github.j_yuhanwang.food_ordering_app.dish.services;

import io.github.j_yuhanwang.food_ordering_app.canteen.entity.Canteen;
import io.github.j_yuhanwang.food_ordering_app.canteen.repository.CanteenRepository;
import io.github.j_yuhanwang.food_ordering_app.dish.dtos.DishDTO;
import io.github.j_yuhanwang.food_ordering_app.dish.entity.Dish;
import io.github.j_yuhanwang.food_ordering_app.dish.mapper.DishMapper;
import io.github.j_yuhanwang.food_ordering_app.dish.repository.DishRepository;
import io.github.j_yuhanwang.food_ordering_app.exceptions.BadRequestException;
import io.github.j_yuhanwang.food_ordering_app.exceptions.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author YuhanWang
 * @Date 25/03/2026 10:09 am
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DishServiceImpl implements DishService{
    private final DishRepository dishRepository;
    private final CanteenRepository canteenRepository;
    private final DishMapper dishMapper;


    @Override
    public DishDTO createDish(Long canteenId, DishDTO dishDTO) {
        log.info("Attempting to create dish to canteen id: {}",canteenId);
        Canteen canteen = findCanteenOrThrow(canteenId);

        validateDishExists(canteenId,dishDTO.getName());

        Dish dish = dishMapper.toEntity(dishDTO);
        dish.setCanteen(canteen);

        Dish savedDish = dishRepository.save(dish);

        return dishMapper.toDTO(savedDish);
    }

    @Override
    public DishDTO updateDishById(Long dishId, DishDTO dishDTO) {
        log.info("Attempting to update dish with id {}",dishId);

        Dish existingDish = findDishOrThrow(dishId);
        //If the dish name has been changed, check if the new name already exists in the current canteen.
        if(!existingDish.getName().equalsIgnoreCase(dishDTO.getName())){
            validateDishExists(existingDish.getCanteen().getId(),dishDTO.getName());
        }
        existingDish.setName(dishDTO.getName());
        existingDish.setDescription(dishDTO.getDescription());
        existingDish.setPrice(dishDTO.getPrice());
        existingDish.setFoodCategory(dishDTO.getFoodCategory());
        existingDish.setAvailable(dishDTO.isAvailable());
        //if frontend has a new imageUrl, rewrite the original url
        if(dishDTO.getImageUrl()!=null && !dishDTO.getImageUrl().isBlank()){
            existingDish.setImageUrl(dishDTO.getImageUrl());
        }

        Dish updatedDish = dishRepository.save(existingDish);
        return dishMapper.toDTO(updatedDish);
    }

    @Override
    public DishDTO getDishById(Long dishId) {
        log.info("Attempting to get dish with id {}",dishId);
        Dish dish = findDishOrThrow(dishId);
        return dishMapper.toDTO(dish);
    }

    @Override
    public void deleteDish(Long dishId) {
        log.info("Attempting to delete dish with id {}",dishId);
        Dish dish = findDishOrThrow(dishId);
        dishRepository.delete(dish);
        log.info("Successfully delete the dish.");
    }

    @Override
    public List<DishDTO> getDishes(Long canteenId, String search) {
        log.info("Fetching dishes for canteen: {}, keyword: {}", canteenId, search);

        findCanteenOrThrow(canteenId);
        List<Dish> dishes;

        if (search != null && !search.trim().isEmpty()) {
            dishes = dishRepository.findByCanteenIdAndNameContainingIgnoreCase(canteenId, search);
        } else {
            dishes = dishRepository.findByCanteenId(canteenId);
        }

        return dishes.stream()
                .map(dishMapper::toDTO)
                .toList();
    }

    //-----------private utility methods------------
    private Canteen findCanteenOrThrow(Long canteenId){
        return canteenRepository.findByIdAndIsDeletedFalse(canteenId).orElseThrow(
                ()->new ResourceNotFoundException("Canteen","id",canteenId)
        );
    }

    private Dish findDishOrThrow(Long dishId){
        return dishRepository.findById(dishId).orElseThrow(
                ()-> new ResourceNotFoundException("Dish","id",dishId)
        );
    }

    //Verify whether the dish names are unique within the specific restaurant.
    private void validateDishExists(Long canteenId, String dishName){
        dishRepository.findByCanteenIdAndName(canteenId,dishName).ifPresent(s->{
            throw new BadRequestException("A dish with name '" + dishName + "' already exists in this canteen.");
        });
    }


}
