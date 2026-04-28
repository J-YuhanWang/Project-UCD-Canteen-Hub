package io.github.j_yuhanwang.food_ordering_app.order.services;

import io.github.j_yuhanwang.food_ordering_app.auth_users.entity.User;
import io.github.j_yuhanwang.food_ordering_app.auth_users.services.UserService;
import io.github.j_yuhanwang.food_ordering_app.canteen.entity.Canteen;
import io.github.j_yuhanwang.food_ordering_app.canteen.repository.CanteenRepository;
import io.github.j_yuhanwang.food_ordering_app.cart.entity.Cart;
import io.github.j_yuhanwang.food_ordering_app.cart.entity.CartItem;
import io.github.j_yuhanwang.food_ordering_app.cart.repository.CartRepository;
import io.github.j_yuhanwang.food_ordering_app.dish.entity.Dish;
import io.github.j_yuhanwang.food_ordering_app.enums.OrderStatus;
import io.github.j_yuhanwang.food_ordering_app.enums.PaymentStatus;
import io.github.j_yuhanwang.food_ordering_app.exceptions.BadRequestException;
import io.github.j_yuhanwang.food_ordering_app.exceptions.ResourceNotFoundException;
import io.github.j_yuhanwang.food_ordering_app.order.dtos.OrderDTO;
import io.github.j_yuhanwang.food_ordering_app.order.entity.Order;
import io.github.j_yuhanwang.food_ordering_app.order.entity.OrderItem;
import io.github.j_yuhanwang.food_ordering_app.order.mapper.OrderItemMapper;
import io.github.j_yuhanwang.food_ordering_app.order.mapper.OrderMapper;
import io.github.j_yuhanwang.food_ordering_app.order.repository.OrderItemRepository;
import io.github.j_yuhanwang.food_ordering_app.order.repository.OrderRepository;
import io.github.j_yuhanwang.food_ordering_app.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * @author YuhanWang
 * @Date 04/04/2026 4:27 pm
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;
    private final UserService userService;
    private final CartRepository cartRepository;
    private final CanteenRepository canteenRepository;

    //1.create the order from the user's cart(core logic)
    @Override
    @Transactional
    public OrderDTO placeOrderFromCart() {
        log.info("Attempting to place order from the current user's cart.");
        //1.check the current user, get current user's cart,if the cart not exists, throw the exception
        User user = userService.getCurrentLoggedInUser();
        Cart cart = cartRepository.findByUserId(user.getId()).orElseThrow(
                () -> new ResourceNotFoundException("Cart", "userId", user.getId())
        );
        //2.check whether the cart is empty
        if (cart.getCartItems() == null || cart.getCartItems().isEmpty()) {
            throw new BadRequestException("Cannot place an order with an empty cart.");
        }
        //3.compose all the elements into the new order object,
        // set User、Canteen、OrderStatus(PENDING)、PaymentStatus(UNPAID)、PickupCode, and totalPrice(0)
        Order order = Order.builder()
                .user(user)
                .canteen(cart.getCartItems().getFirst().getDish().getCanteen())
                .orderDate(LocalDateTime.now())
                .orderStatus(OrderStatus.INITIALIZED)
                .paymentStatus(PaymentStatus.PENDING)
                .totalAmount(BigDecimal.ZERO)
                .pickupCode(generatePickupCode())
                .build();

        //4.iterate the cartItem and convert them to orderItem, calculate the total price
        BigDecimal calculatedTotal = BigDecimal.ZERO;
        for (CartItem cartItem : cart.getCartItems()) {
            Dish dish = cartItem.getDish();
            BigDecimal subtotal = dish.getPrice().multiply(BigDecimal.valueOf(cartItem.getQuantity()));
            OrderItem orderItem = OrderItem.builder()
                    .order(order)
                    .dish(dish)
                    .dishName(dish.getName())
                    .dishImageUrl(dish.getImageUrl())
                    .quantity(cartItem.getQuantity())
                    .pricePerUnit(dish.getPrice())
                    .subtotal(subtotal)
                    .build();
            //put items to the order
            order.getOrderItems().add(orderItem);
            //add the totalAmount
            calculatedTotal = calculatedTotal.add(subtotal);
        }
        order.setTotalAmount(calculatedTotal);

        //5.save the order and clear the cart
        Order savedOrder = orderRepository.save(order);
        log.info("Order created successfully with ID: {}", savedOrder.getId());

        cart.getCartItems().clear();
        cartRepository.save(cart);
        log.info("Cart cleared for user ID: {}", user.getId());

        return orderMapper.toDTO(savedOrder);
    }

    //1 helper function
    private String generatePickupCode() {
        return UUID.randomUUID().toString().substring(0, 4).toUpperCase();
    }

    //2.Query methods
    //2.1 users themselves/admin/valid manager can query the specific order
    @Override
    @Transactional(readOnly = true)
    public OrderDTO getOrderById(Long orderId) {
        log.info("Attempting to get order information by order ID:{}", orderId);
        Order order = orderRepository.findById(orderId).orElseThrow(
                () -> new ResourceNotFoundException("Order", "orderId", orderId)
        );
        //authentication
        User currentUser = userService.getCurrentLoggedInUser();
        boolean isOwner = currentUser.getId().equals(order.getUser().getId());
        boolean isValidManager = currentUser.isManager() &&
                order.getCanteen().getManager().getId().equals(currentUser.getId());
        //
        if (!isOwner && !currentUser.isAdmin() && !isValidManager) {
            throw new BadRequestException("You are not authorized to view this order.");
        }

        return orderMapper.toDTO(order);
    }

    //2.2 user themselves can query their own orders
    @Override
    @Transactional(readOnly = true)
    public Page<OrderDTO> getOrdersOfUser(int page, int size) {
        log.info("User attempting to fetch their own orders");
        User user = userService.getCurrentLoggedInUser();
        Pageable pageable = createPageRequest(page,size);
        Page<Order> orderPage = orderRepository.findByUserId(user.getId(), pageable);
        return orderPage.map(orderMapper::toDTO);
    }

    //2.3 manager and admin can query the specific canteen's orders
    @Override
    @Transactional(readOnly = true)
    public Page<OrderDTO> getOrdersByCanteenId(Long canteenId, OrderStatus status, int page, int size) {
        log.info("Manager attempting to fetch orders for Canteen ID: {}", canteenId);
        Canteen canteen = canteenRepository.findByIdAndIsDeletedFalse(canteenId).orElseThrow(
                () -> new ResourceNotFoundException("Canteen", "canteenId", canteenId)
        );
        //authentication
        boolean isValidManager = canteen.getManager().getEmail().equals(SecurityUtils.getCurrentUserEmail());
        boolean isAdmin = SecurityUtils.isAdmin();
        if (!isValidManager && !isAdmin) {
            throw new BadRequestException("You are not authorized to view this canteen's orders.");
        }

        //fetch the orders
        Pageable pageable = createPageRequest(page,size);
        Page<Order> orderPage;
        if (status != null) {
            orderPage = orderRepository.findByCanteenIdAndOrderStatus(canteenId, status, pageable);
        } else {
            orderPage = orderRepository.findByCanteenId(canteenId, pageable);
        }

        return orderPage.map(orderMapper::toDTO);
    }

    //2.4 only the administrators can query all orders
    @Override
    @Transactional(readOnly = true)
    public Page<OrderDTO> getAllOrders(OrderStatus orderStatus, int page, int size) {
        log.info("Administrator attempting to fetch all orders for {}", orderStatus);
        if (!SecurityUtils.isAdmin()) {
            throw new BadRequestException("You are not authorized to view all orders");
        }
        Pageable pageable = createPageRequest(page,size);
        Page<Order> orderPage;
        if (orderStatus != null) {
            orderPage = orderRepository.findByOrderStatus(orderStatus, pageable);
        } else {
            orderPage = orderRepository.findAll(pageable);
        }
//        Page<OrderDTO> orderDTOPage=orderPage.map(order->{
//            return orderMapper.toDTO(order);
//        });

        return orderPage.map(orderMapper::toDTO);
    }

    //3.change the status
    @Override
    @Transactional
    public OrderDTO updateOrderStatus(Long orderId, OrderStatus newStatus) {
        log.info("Attempting to update status for order: {}", orderId);
        //3.1 get the order
        Order order = orderRepository.findById(orderId).orElseThrow(
                () -> new ResourceNotFoundException("Order", "orderId", orderId)
        );
        OrderStatus currentStatus = order.getOrderStatus();
        User user = userService.getCurrentLoggedInUser();

        //3.2 State Transition Validation, check whether currentStatus can transfer to newStatus
        if (!isValidTransition(currentStatus, newStatus)) {
            throw new BadRequestException("Invalid status transition from " + currentStatus + " to " + newStatus);
        }

        //3.3 Role-Based Access Control validation
        validateOperatorPermission(user, order, newStatus);

        //3.4 Business Side Effects
        handleSideEffects(order, newStatus);

        //3.5 update orderStatus and save it
        order.setOrderStatus(newStatus);
        Order updatedOrder = orderRepository.save(order);

        return orderMapper.toDTO(updatedOrder);
    }

    //helper method 3.4: order->payment, about the refund
    private void handleSideEffects(Order order, OrderStatus newStatus) {
        //1). order is cancelled
        if (newStatus == OrderStatus.CANCELLED) {
            // check the old status: whether finish the payment? CONFIRMED or READY_FOR_PICKUP
            OrderStatus oldStatus = order.getOrderStatus();
            //Cancelled after CONFIRMED: A refund will only be issued if the order is cancelled while it is being prepared (CONFIRMED).
            if (oldStatus == OrderStatus.CONFIRMED) {
                log.info("Order {} cancelled during preparation. Triggering refund process...", order.getId());
                // TODO: paymentService.refund(order.getId());
                //Cancelled after READY_FOR_PICKUP: No refunds will be given (students are responsible for any losses).
            } else if (oldStatus == OrderStatus.READY_FOR_PICKUP) {
                log.warn("Order {} cancelled after food was ready (No-show). No refund issued.", order.getId());
            }
            // Cancelled before CONFIRMED(payment)
            else {
                log.info("Order {} cancelled before payment. No refund needed.", order.getId());
            }
        }
        // 2). Payment successfully -> CONFIRMED
        if (newStatus == OrderStatus.CONFIRMED) {
            log.info("Order {} confirmed. TODO: Notify canteen kitchen dashboard to start cooking.", order.getId());
        }

        // 3). The meal is finished -> READY_FOR_PICKUP
        if (newStatus == OrderStatus.READY_FOR_PICKUP) {
            log.info("Order {} is ready for pickup. TODO: Send email/notification to student.", order.getId());
        }

        // 4). COMPLETED -> Transaction closed
        if (newStatus == OrderStatus.COMPLETED) {
            log.info("Order {} successfully picked up by student.", order.getId());
        }
    }

    //helper method 3.3: validate if current user has the authorization to operate the order
    private void validateOperatorPermission(User user, Order order, OrderStatus newStatus) {
        //1.admin
        if (user.isAdmin()) {
            return;
        }
        //2.manager
        if (user.isManager()) {
            //Horizontal defense: Managers can only modify orders for the cafeterias they manage.
            boolean belongsToManager = user.getId().equals(order.getCanteen().getManager().getId());
            if (!belongsToManager) {
                throw new BadRequestException("You don't manage this canteen.");
            }
        }
        //3.user
        if (user.isStudent()) {
            //1) Horizontal defense: Users can only modify their own orders.
            if (!order.getUser().getId().equals(user.getId())) {
                throw new BadRequestException("Not your order.");
            }
            //2)Users can only perform cancellation operations.
            if (newStatus != OrderStatus.CANCELLED) {
                throw new BadRequestException("Users are only allowed to cancel orders.");
            }
            //3)Cancellation operations can only be performed under the INITIALIZED status
            if (order.getOrderStatus() != OrderStatus.INITIALIZED) {
                throw new BadRequestException("Order is already processing. Contact canteen to cancel and refund.");
            }
        }
    }

    //helper method 3.2: status transition validation
    private boolean isValidTransition(OrderStatus from, OrderStatus to) {
        return switch (from) {
            case INITIALIZED -> List.of(OrderStatus.CONFIRMED, OrderStatus.CANCELLED, OrderStatus.FAILED).contains(to);
            case CONFIRMED -> List.of(OrderStatus.READY_FOR_PICKUP, OrderStatus.CANCELLED).contains(to);
            case READY_FOR_PICKUP -> List.of(OrderStatus.COMPLETED, OrderStatus.CANCELLED).contains(to);
            case COMPLETED, CANCELLED, FAILED -> false;
        };
    }


    //cancel order by user actively
    @Override
    @Transactional
    public void cancelOrder(Long orderId) {
        log.info("User requested to cancel order: {}", orderId);
        updateOrderStatus(orderId, OrderStatus.CANCELLED);
    }

    //Cron job: Timed scanning method( waiting for 15 minutes, do not convey to frontend)
    @Override
    @Transactional
    @Scheduled(cron = "0 * * * * ?")
    public void cancelUnpaidOrders() {
        //1.find the order initialised 15 minutes before and killed
        LocalDateTime cutoffTime = LocalDateTime.now().minusMinutes(15);
        List<Order> unpaidOrders = orderRepository.findByOrderStatusAndOrderDateBefore(OrderStatus.INITIALIZED, cutoffTime);
        if (unpaidOrders.isEmpty()) {
            return;
        }
        //2.modified the scanned unpaid orders status to 'FAILED'
        log.info("Found {} unpaid orders to be auto-cancelled.", unpaidOrders.size());
        for (Order unpaidOrder : unpaidOrders) {
            unpaidOrder.setOrderStatus(OrderStatus.FAILED);
        }
        //3. save the change status orders to repo
        orderRepository.saveAll(unpaidOrders);
    }

    @Override
    public long countUniqueCustomers() {
        if (!SecurityUtils.isAdmin()) {
            throw new BadRequestException("Only administrators can access customer statistics.");
        }

        log.info("Attempting to count the unique customers");
        return orderRepository.countDistinctUsers();
    }

    @Override
    public BigDecimal getRevenueByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        if (!SecurityUtils.isAdmin()) {
            throw new BadRequestException("Only administrators can access revenue data.");
        }

        log.info("Attempting to get the revenue by date range from {} to {}", startDate, endDate);
        BigDecimal revenue = orderRepository.calculateRevenueByDateRange(startDate, endDate);
        return revenue != null ? revenue : BigDecimal.ZERO;
    }

    // Standardize parameters and sort them in descending order by ID (newest first) by default.
    private Pageable createPageRequest(int page, int size) {
        return PageRequest.of(
                Math.max(0, page),
                Math.max(1, Math.min(size, 100)),
                Sort.by(Sort.Direction.DESC, "id")
        );
    }
}
