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
import io.github.j_yuhanwang.food_ordering_app.enums.RoleType;
import io.github.j_yuhanwang.food_ordering_app.exceptions.BadRequestException;
import io.github.j_yuhanwang.food_ordering_app.exceptions.ResourceNotFoundException;
import io.github.j_yuhanwang.food_ordering_app.order.dtos.OrderDTO;
import io.github.j_yuhanwang.food_ordering_app.order.dtos.OrderItemDTO;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
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
                ()->new ResourceNotFoundException("Cart","userId",user.getId())
        );
        //2.check whether the cart is empty
        if(cart.getCartItems()==null || cart.getCartItems().isEmpty()){
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
        for(CartItem cartItem: cart.getCartItems()){
            Dish dish = cartItem.getDish();
            BigDecimal subtotal = dish.getPrice().multiply(cartItem.getSubtotal());
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

    //helper function
    private String generatePickupCode() {
        return UUID.randomUUID().toString().substring(0,4).toUpperCase();
    }

    //2.Query methods
    //2.1 users themselves/admin/valid manager can query the specific order
    @Override
    public OrderDTO getOrderById(Long orderId) {
        log.info("Attempting to get order information by order ID:{}",orderId);
        Order order = orderRepository.findById(orderId).orElseThrow(
                ()->new ResourceNotFoundException("Order","orderId",orderId)
        );
        //authentication
        User currentUser = userService.getCurrentLoggedInUser();
        boolean isOwner = currentUser.getId().equals(order.getUser().getId());
        boolean isValidManager = currentUser.isManager() &&
                order.getCanteen().getManager().getId().equals(currentUser.getId());
        //
        if(!isOwner && !currentUser.isAdmin() && !isValidManager){
            throw new BadRequestException("You are not authorized to view this order.");
        }

        return orderMapper.toDTO(order);
    }

    //2.2 user themselves can query their own orders
    @Override
    public Page<OrderDTO> getOrdersOfUser(int page, int size) {
        log.info("User attempting to fetch their own orders");
        User user = userService.getCurrentLoggedInUser();
        Pageable pageable = PageRequest.of(page,size,Sort.by(Sort.Direction.DESC,"id"));
        Page<Order> orderPage = orderRepository.findByUserId(user.getId(),pageable);
        return orderPage.map(orderMapper::toDTO);
    }

    //2.3 manager and admin can query the specific canteen's orders
    @Override
    public Page<OrderDTO> getOrdersByCanteenId(Long canteenId, OrderStatus status, int page, int size) {
        log.info("Manager attempting to fetch orders for Canteen ID: {}", canteenId);
        Canteen canteen = canteenRepository.findByIdAndIsDeletedFalse(canteenId).orElseThrow(
                ()->new ResourceNotFoundException("Canteen","canteenId",canteenId)
        );
        //authentication
        boolean isValidManager = canteen.getManager().getEmail().equals(SecurityUtils.getCurrentUserEmail());
        boolean isAdmin = SecurityUtils.isAdmin();
        if(!isValidManager && !isAdmin){
            throw new BadRequestException("You are not authorized to view this canteen's orders.");
        }

        //fetch the orders
        Pageable pageable = PageRequest.of(page,size,Sort.by(Sort.Direction.DESC,"id"));
        Page<Order> orderPage;
        if(status!=null){
            orderPage = orderRepository.findByCanteenIdAndOrderStatus(canteenId,status,pageable);
        }else{
            orderPage = orderRepository.findByCanteenId(canteenId,pageable);
        }

        return orderPage.map(orderMapper::toDTO);
    }

    //2.4 only the administrators can query all orders
    @Override
    public Page<OrderDTO> getAllOrders(OrderStatus orderStatus, int page, int size) {
        log.info("Administrator attempting to fetch all orders for {}", orderStatus);
        if(!SecurityUtils.isAdmin()){
            throw new BadRequestException("You are not authorized to view all orders");
        }
        Pageable pageable = PageRequest.of(page,size,Sort.by(Sort.Direction.DESC, "id"));
        Page<Order> orderPage;
        if(orderStatus!=null){
            orderPage = orderRepository.findByOrderStatus(orderStatus,pageable);
        }else{
            orderPage = orderRepository.findAll(pageable);
        }
//        Page<OrderDTO> orderDTOPage=orderPage.map(order->{
//            return orderMapper.toDTO(order);
//        });

        return orderPage.map(orderMapper::toDTO);
    }



    @Override
    public OrderDTO updateOrderStatus(Long orderId, OrderStatus status) {
        return null;
    }

    @Override
    public void cancelUnpaidOrders() {

    }

    //cancel order by user actively
    @Override
    public void cancelOrder(Long orderId) {

    }

    @Override
    public int countUniqueCustomers() {
        return 0;
    }

    @Override
    public BigDecimal getRevenueByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return null;
    }
}
