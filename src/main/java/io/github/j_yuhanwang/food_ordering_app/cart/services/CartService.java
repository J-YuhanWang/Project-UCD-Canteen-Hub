package io.github.j_yuhanwang.food_ordering_app.cart.services;

import io.github.j_yuhanwang.food_ordering_app.cart.dtos.CartDTO;

/**
 * @author YuhanWang
 * @Date 25/03/2026 9:30 am
 */
public interface CartService {
    //Responsible for cross-store validation, creating new items or accumulating quantities, and calculating subtotal.
    CartDTO addItemToCart(Long dishId, Integer quantity);

    //increment quantity and update subtotal.
    CartDTO incrementItem(Long cartItemId);

    //decrement quantity; if it reaches 0, it triggers removeCartItem.
    CartDTO decrementItem(Long cartItemId);

    //remove items from the cartItems list (triggers orphanRemoval).
    void removeCartItem(Long cartItemId);

    //Responsible for calculating the total price, deriving canteenId, and assembling the DTO.
    CartDTO getShoppingCart();

    //Responsible for clearing cart.getCartItems().clear().
    void clearCart();
}
