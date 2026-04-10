package io.github.j_yuhanwang.food_ordering_app.review.mapper;

import io.github.j_yuhanwang.food_ordering_app.review.dtos.ReviewDTO;
import io.github.j_yuhanwang.food_ordering_app.review.entity.Review;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

/**
 * @author YuhanWang
 * @Date 08/04/2026 10:43 am
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ReviewMapper {
    @Mapping(source = "user.id", target = "userId")
    @Mapping(source = "user.name", target = "userName")
    @Mapping(source = "user.profileUrl", target = "userAvatarUrl")
    @Mapping(source = "dish.id", target = "dishId")
    @Mapping(source = "dish.name", target = "dishName")
    @Mapping(source = "dish.imageUrl", target = "dishImageUrl")
    ReviewDTO toDTO(Review review);

    @Mapping(target = "user", ignore = true)
    @Mapping(target = "dish", ignore = true)
    Review toEntity(ReviewDTO reviewDTO);

}
