package io.github.j_yuhanwang.food_ordering_app.review.services;

import io.github.j_yuhanwang.food_ordering_app.review.dtos.ReviewDTO;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * @author YuhanWang
 * @Date 07/04/2026 9:56 pm
 */
public interface ReviewService {
    ReviewDTO createReview(ReviewDTO reviewDTO);
    Page<ReviewDTO> getReviewsForDish(Long dishId, int page, int size);
    Page<ReviewDTO> getMyReviews(int page, int size);
    Double getAverageRating(Long dishId);
}
