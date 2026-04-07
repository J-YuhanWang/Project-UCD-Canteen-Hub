package io.github.j_yuhanwang.food_ordering_app.order.controller;

import io.github.j_yuhanwang.food_ordering_app.enums.OrderStatus;
import io.github.j_yuhanwang.food_ordering_app.order.dtos.OrderDTO;
import io.github.j_yuhanwang.food_ordering_app.order.services.OrderService;
import io.github.j_yuhanwang.food_ordering_app.response.Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * @author YuhanWang
 * @Date 06/04/2026 8:25 pm
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("api/v1/orders")
@Slf4j
public class OrderController {
    private final OrderService orderService;

    //1.create the order
    @PostMapping
    @PreAuthorize("hasRole('ROLE_STUDENT')")
    public Response<OrderDTO> placeOrderFromCart(){
        return Response.ok(orderService.placeOrderFromCart());
    }

    //2.query the orders
    @GetMapping("/{orderId}")
    @PreAuthorize("isAuthenticated()")
    public Response<OrderDTO> getOrderById(@PathVariable Long orderId){
        return Response.ok(orderService.getOrderById(orderId));
    }

    @GetMapping("/my-orders")
    @PreAuthorize("hasRole('ROLE_STUDENT')")
    public Response<Page<OrderDTO>> getMyOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size){
        return Response.ok(orderService.getOrdersOfUser(page,size));
    }

    @GetMapping("/canteens/{canteenId}")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MANAGER')")
    public Response<Page<OrderDTO>> getOrdersByCanteenId(
            @PathVariable Long canteenId,
            @RequestParam(required = false) OrderStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10")int size){
        return Response.ok(orderService.getOrdersByCanteenId(canteenId,status,page,size));
    }

    @GetMapping("/all")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public Response<Page<OrderDTO>> getAllOrders(
            @RequestParam(required = false) OrderStatus orderStatus,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size){
        return Response.ok(orderService.getAllOrders(orderStatus,page,size));
    }

    //3. change the status
    @PutMapping("/{orderId}/status")
    @PreAuthorize("isAuthenticated()")
    public Response<OrderDTO> updateOrderStatus(
            @PathVariable Long orderId,
            @RequestParam OrderStatus status){
        return Response.ok(orderService.updateOrderStatus(orderId,status));
    }

    @PostMapping("/{orderId}/cancel")
    @PreAuthorize("hasRole('ROLE_STUDENT')")
    public Response<String> cancelOrder(
            @PathVariable Long orderId){
        orderService.cancelOrder(orderId);
        return Response.ok("The order is canceled successfully.");
    }

    //4.Admin Stats
    @GetMapping("/admin/stats/customers/count")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public Response<Long> getCountUniqueCustomers(){
        return Response.ok(orderService.countUniqueCustomers());
    }

    @GetMapping("/admin/stats/revenue")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public Response<BigDecimal> getRevenueByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate){
        return Response.ok(orderService.getRevenueByDateRange(startDate, endDate));
    }
}
