package io.github.j_yuhanwang.food_ordering_app.review.controller;

import io.github.j_yuhanwang.food_ordering_app.response.Response;
import io.github.j_yuhanwang.food_ordering_app.review.dtos.ReviewDTO;
import io.github.j_yuhanwang.food_ordering_app.review.services.ReviewService;
import jakarta.validation.Valid;
import jakarta.websocket.server.PathParam;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * @author YuhanWang
 * @Date 08/04/2026 12:15 pm
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/reviews")
@Slf4j
public class ReviewController {
    private final ReviewService reviewService;

    //1.create the review
    @PostMapping
    @PreAuthorize("hasRole('ROLE_STUDENT')")
    public Response<ReviewDTO> createReview(@RequestBody @Valid ReviewDTO reviewDTO){
        return Response.ok(reviewService.createReview(reviewDTO));
    }

    //2.query the reviews
    @GetMapping("/dish/{dishId}")
    @PreAuthorize("isAuthenticated()")
    public Response<Page<ReviewDTO>> getReviewsForDish(
            @PathVariable Long dishId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size){
        return Response.ok(reviewService.getReviewsForDish(dishId,page,size));
    }

    @GetMapping("/me")
    @PreAuthorize("hasRole('ROLE_STUDENT')")
    public Response<Page<ReviewDTO>> getMyReviews(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size){
        return Response.ok(reviewService.getMyReviews(page,size));
    }

    //3.get average rating for specific order
    @GetMapping("/dish/{dishId}/rating")
    public Response<Double> getAverageRating(@PathVariable Long dishId){
        return Response.ok(reviewService.getAverageRating(dishId));
    }

}
