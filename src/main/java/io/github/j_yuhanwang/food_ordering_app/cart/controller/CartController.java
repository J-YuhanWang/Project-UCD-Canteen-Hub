package io.github.j_yuhanwang.food_ordering_app.cart.controller;

import io.github.j_yuhanwang.food_ordering_app.cart.dtos.CartDTO;
import io.github.j_yuhanwang.food_ordering_app.cart.services.CartService;
import io.github.j_yuhanwang.food_ordering_app.response.Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * @author YuhanWang
 * @Date 27/03/2026 9:34 pm
 */
@RestController
@RequestMapping("/api/v1/cart")
@RequiredArgsConstructor
@Slf4j
public class CartController {
    private final CartService cartService;

    @GetMapping
    public Response<CartDTO> getShoppingCart(){
        CartDTO dto = cartService.getShoppingCart();
        return Response.ok(dto);
    }

    // PUTMapping is idempotent, change once equals to change 100 times,such as change the password;
    // PostMapping is non-idempotent
    @PostMapping("/items/{dishId}")
    public Response<CartDTO> addItemToCart(
            @PathVariable Long dishId,
            @RequestParam(defaultValue = "1") Integer quantity){
        CartDTO dto = cartService.addItemToCart(dishId,quantity);
        return Response.ok(dto);
    }

    //PatchMapping = partial update
    @PatchMapping("/items/{cartItemId}/increment")
    public Response<CartDTO> incrementItem(@PathVariable Long cartItemId){
        CartDTO dto = cartService.incrementItem(cartItemId);
        return Response.ok(dto);
    }

    @PatchMapping("/items/{cartItemId}/decrement")
    public Response<CartDTO> decrementItem(@PathVariable Long cartItemId){
        CartDTO dto = cartService.decrementItem(cartItemId);
        return Response.ok(dto);
    }

    @DeleteMapping("/items/{cartItemId}")
    public Response<String> removeItem(@PathVariable Long cartItemId){
        cartService.removeCartItem(cartItemId);
        return Response.ok("Successfully remove the item from cart");
    }

    @DeleteMapping
    public Response<String> clearCart(){
        cartService.clearCart();
        return Response.ok("Successfully clear the shopping cart");
    }

}
