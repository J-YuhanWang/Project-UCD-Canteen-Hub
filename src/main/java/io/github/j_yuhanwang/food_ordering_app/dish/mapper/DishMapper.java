package io.github.j_yuhanwang.food_ordering_app.dish.mapper;

import io.github.j_yuhanwang.food_ordering_app.dish.dtos.DishDTO;
import io.github.j_yuhanwang.food_ordering_app.dish.entity.Dish;
import io.github.j_yuhanwang.food_ordering_app.dish.repository.DishRepository;
import io.github.j_yuhanwang.food_ordering_app.review.entity.Review;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * @author YuhanWang
 * @Date 25/03/2026 10:14 am
 */
@Component
public class DishMapper {

    public DishDTO toDTO(Dish entity){
        if(entity == null){
            return null;
        }
        //canteen id & canteen name
        Long cId = entity.getCanteen()!=null ? entity.getCanteen().getId():null;
        String cName = entity.getCanteen()!=null? entity.getCanteen().getName(): null;

        int count = 0;
        double avgRating = 0.0;
        if(entity.getReviews()!=null && !entity.getReviews().isEmpty()){
            count = entity.getReviews().size();
            avgRating = entity.getReviews().stream()
                    .mapToInt(Review::getRating)
                    .average()
                    .orElse(0.0);
        }

        return DishDTO.builder()
                .id(entity.getId())
                .name(entity.getName())
                .description(entity.getDescription())
                .price(entity.getPrice())
                .imageUrl(entity.getImageUrl())
                .foodCategory(entity.getFoodCategory())
                .isAvailable(entity.isAvailable())

                .canteenId(cId)
                .canteenName(cName)
                .reviewCount(count)
                .averageRating(avgRating)

                .build();
    }

    public Dish toEntity(DishDTO dto){
        if(dto == null){
            return null;
        }
        return Dish.builder()
                .name(dto.getName())
                .description(dto.getDescription())
                .price(dto.getPrice())
                .imageUrl(dto.getImageUrl())
                .foodCategory(dto.getFoodCategory())
                .isAvailable(dto.isAvailable())
                .build();
    }
}

