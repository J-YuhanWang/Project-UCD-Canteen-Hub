package io.github.j_yuhanwang.food_ordering_app.dish.services;

import io.github.j_yuhanwang.food_ordering_app.dish.dtos.DishDTO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * @author YuhanWang
 * @Date 25/03/2026 8:42 am
 */
public interface DishService {
    DishDTO createDish(Long canteenId, DishDTO dishDTO);
    DishDTO updateDishById(Long dishId, DishDTO dishDTO);
    DishDTO getDishById(Long dishId);
    void deleteDish(Long dishId);
    List<DishDTO> getDishes(Long canteenId, String search);
}
