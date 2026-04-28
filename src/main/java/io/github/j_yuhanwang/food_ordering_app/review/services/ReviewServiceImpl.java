package io.github.j_yuhanwang.food_ordering_app.review.services;

import io.github.j_yuhanwang.food_ordering_app.auth_users.entity.User;
import io.github.j_yuhanwang.food_ordering_app.auth_users.services.UserService;
import io.github.j_yuhanwang.food_ordering_app.dish.entity.Dish;
import io.github.j_yuhanwang.food_ordering_app.dish.repository.DishRepository;
import io.github.j_yuhanwang.food_ordering_app.enums.OrderStatus;
import io.github.j_yuhanwang.food_ordering_app.exceptions.BadRequestException;
import io.github.j_yuhanwang.food_ordering_app.exceptions.ResourceNotFoundException;
import io.github.j_yuhanwang.food_ordering_app.order.entity.Order;
import io.github.j_yuhanwang.food_ordering_app.order.repository.OrderItemRepository;
import io.github.j_yuhanwang.food_ordering_app.order.repository.OrderRepository;
import io.github.j_yuhanwang.food_ordering_app.review.dtos.ReviewDTO;
import io.github.j_yuhanwang.food_ordering_app.review.entity.Review;
import io.github.j_yuhanwang.food_ordering_app.review.mapper.ReviewMapper;
import io.github.j_yuhanwang.food_ordering_app.review.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author YuhanWang
 * @Date 08/04/2026 10:42 am
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService{
    private final ReviewRepository reviewRepository;
    private final DishRepository dishRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final ReviewMapper reviewMapper;
    private final UserService userService;

    @Override
    public ReviewDTO createReview(ReviewDTO reviewDTO) {
        log.info("Attempting to create review");
        //get current user
        User user = userService.getCurrentLoggedInUser();

        //validate required fields
        if(reviewDTO.getOrderId()==null || reviewDTO.getDishId()==null){
            throw new BadRequestException("Order ID and Dish ID are required");
        }
        //validate dish items exists
        Dish dish = dishRepository.findById(reviewDTO.getDishId()).orElseThrow(
                ()->new ResourceNotFoundException("Dish","dishId",reviewDTO.getDishId())
        );
        //validate order exists
        Order order = orderRepository.findById(reviewDTO.getOrderId()).orElseThrow(
                ()->new ResourceNotFoundException("Order","orderId",reviewDTO.getOrderId())
        );

        //make sure the order belongs to you
        if(!order.getUser().getId().equals(user.getId())){
            throw new BadRequestException("This order does not belong to you.");
        }
        //validate order status is COMPLETED
        if(order.getOrderStatus()!= OrderStatus.COMPLETED){
            throw new BadRequestException("You can only review items from completed orders.");
        }
        //validate that dish item was part of this order
        boolean itemInOrder = orderItemRepository.existsByOrderIdAndDishId(order.getId(),dish.getId());
        if(!itemInOrder){
            throw new BadRequestException("This dish item was not part of the specific order");
        }
        //check if user already wrote a review for this order
        if(reviewRepository.existsByUserIdAndDishIdAndOrderId(user.getId(),dish.getId(),order.getId())){
            throw new BadRequestException("You've already reviewed this item from this order.");
        }
        Review review = Review.builder()
                .user(user)
                .dish(dish)
                .orderId(order.getId())
                .rating(reviewDTO.getRating())
                .comment(reviewDTO.getComment())
                .build();
        Review savedReview = reviewRepository.save(review);

        return reviewMapper.toDTO(savedReview);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ReviewDTO> getReviewsForDish(Long dishId, int page, int size) {
        log.info("Attempting to get reviews from dish:{}",dishId);
        Dish dish = dishRepository.findById(dishId).orElseThrow(
                ()->new ResourceNotFoundException("Dish","dish",dishId)
        );
        Pageable pageable = createPageRequest(page,size);
        Page<Review> reviews=reviewRepository.findByDishIdOrderByCreatedAtDesc(dishId,pageable);
        return reviews.map(reviewMapper::toDTO);
    }

    @Override
    public Page<ReviewDTO> getMyReviews(int page, int size) {
        User user = userService.getCurrentLoggedInUser();
        log.info("Fetching paginated reviews for user ID: {}", user.getId());

        Pageable pageable = createPageRequest(page,size);
        Page<Review> reviews = reviewRepository.findByUserIdOrderByCreatedAtDesc(user.getId(),pageable);

        return reviews.map(reviewMapper::toDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Double getAverageRating(Long dishId) {
        log.info("Attempting to get average rating for dish: {}:", dishId);
        Dish dish = dishRepository.findById(dishId).orElseThrow(
                ()->new ResourceNotFoundException("Dish","dish",dishId)
        );
        Double avgRating = reviewRepository.calculateAverageRatingByDishId(dishId);

        return avgRating != null? avgRating: 0.0;
    }

    private Pageable createPageRequest(int page, int size) {
        return PageRequest.of(
                Math.max(0, page),
                Math.max(1, Math.min(size, 100)),
                Sort.by(Sort.Direction.DESC, "createdAt")
        );
    }
}
