package io.github.j_yuhanwang.food_ordering_app.dish.controller;

import io.github.j_yuhanwang.food_ordering_app.dish.dtos.DishDTO;
import io.github.j_yuhanwang.food_ordering_app.dish.services.DishService;
import io.github.j_yuhanwang.food_ordering_app.response.Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author YuhanWang
 * @Date 25/03/2026 2:43 pm
 */
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Slf4j
public class DishController {
    private final DishService dishService;

    @PostMapping("/canteens/{canteenId}/dishes")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public Response<DishDTO> createDish(
            @PathVariable Long canteenId,
            @RequestBody DishDTO dishDTO){
        DishDTO dto = dishService.createDish(canteenId,dishDTO);
        return Response.ok(dto);
    }

    @PutMapping("/dishes/{dishId}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public Response<DishDTO> updateDishById(
            @PathVariable Long dishId,
            @RequestBody DishDTO dishDTO){
        DishDTO dto = dishService.updateDishById(dishId,dishDTO);
        return Response.ok(dto);
    }

    @GetMapping("/dishes/{dishId}")
    public Response<DishDTO> getDishById(@PathVariable Long dishId){
        return Response.ok(dishService.getDishById(dishId));
    }

    @DeleteMapping("/dishes/{dishId}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public Response<String> deleteDish(@PathVariable Long dishId){
        dishService.deleteDish(dishId);
        return Response.ok("Delete the dish successfully.");
    }

    @GetMapping("/canteens/{canteenId}/dishes")
    public Response<List<DishDTO>> getMenus(
            @PathVariable Long canteenId,
            @RequestParam(required = false) String search){
        List<DishDTO> dishes = dishService.getDishes(canteenId,search);
        return Response.ok(dishes);
    }
}
