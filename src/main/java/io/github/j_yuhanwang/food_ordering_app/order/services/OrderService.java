package io.github.j_yuhanwang.food_ordering_app.order.services;

import io.github.j_yuhanwang.food_ordering_app.enums.OrderStatus;
import io.github.j_yuhanwang.food_ordering_app.order.dtos.OrderDTO;
import io.github.j_yuhanwang.food_ordering_app.order.dtos.OrderItemDTO;
import org.springframework.data.domain.Page;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * @author YuhanWang
 * @Date 04/04/2026 12:56 pm
 */
public interface OrderService {
    //1.create the order(core logic)
    OrderDTO placeOrderFromCart();

    //2. Query methods
    //2.1 users themselves/admin/valid manager can query the specific order
    OrderDTO getOrderById(Long orderId);
    //2.2 user themselves can query their own orders
    Page<OrderDTO> getOrdersOfUser(int page,int size);
    //2.3 manager can query the specific canteen's orders
    Page<OrderDTO> getOrdersByCanteenId(Long canteenId, OrderStatus status, int page, int size);
    //2.4 only the administrators can query all orders
    Page<OrderDTO> getAllOrders(OrderStatus orderStatus, int page, int size);

    //3.change the status
    OrderDTO updateOrderStatus(Long orderId,OrderStatus status);
    //Timed scanning method(for 5 minutes, do not convey to frontend)
    void cancelUnpaidOrders();
    //user cancel the order
    void cancelOrder(Long orderId);

    //4. the aggregate information
    int countUniqueCustomers();
    BigDecimal getRevenueByDateRange(LocalDateTime startDate, LocalDateTime endDate);

}
