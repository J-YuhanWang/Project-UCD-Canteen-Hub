package io.github.j_yuhanwang.food_ordering_app.cart.services;

import io.github.j_yuhanwang.food_ordering_app.auth_users.entity.User;
import io.github.j_yuhanwang.food_ordering_app.auth_users.services.UserService;
import io.github.j_yuhanwang.food_ordering_app.cart.dtos.CartDTO;
import io.github.j_yuhanwang.food_ordering_app.cart.entity.Cart;
import io.github.j_yuhanwang.food_ordering_app.cart.entity.CartItem;
import io.github.j_yuhanwang.food_ordering_app.cart.mapper.CartMapper;
import io.github.j_yuhanwang.food_ordering_app.cart.repository.CartItemRepository;
import io.github.j_yuhanwang.food_ordering_app.cart.repository.CartRepository;
import io.github.j_yuhanwang.food_ordering_app.dish.entity.Dish;
import io.github.j_yuhanwang.food_ordering_app.dish.repository.DishRepository;
import io.github.j_yuhanwang.food_ordering_app.exceptions.BadRequestException;
import io.github.j_yuhanwang.food_ordering_app.exceptions.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Optional;

/**
 * @author YuhanWang
 * @Date 27/03/2026 2:11 pm
 */
@RequiredArgsConstructor
@Service
@Slf4j
@Transactional(rollbackFor = Exception.class)
public class CartServiceImpl implements CartService {
    private final CartMapper cartMapper;
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final UserService userService;
    private final DishRepository dishRepository;

    @Override
    public CartDTO addItemToCart(Long dishId, Integer quantity) {
        log.info("Attempting to add item to cart.");
        //1. get current user
        User user = userService.getCurrentLoggedInUser();

        //2. fetch dish by dish id, check whether dish.isAvailable=true
        Dish dish = dishRepository.findById(dishId).orElseThrow(
                () -> new ResourceNotFoundException("Dish", "id", dishId)
        );
        if (!dish.isAvailable()) {
            throw new BadRequestException("The dish is currently unavailable. It cannot be added to the cart.");
        }
        //3. Cart Discovery, find cart from user, if user does not have cart, new cart()
        Cart cart = cartRepository.findByUserId(user.getId()).orElseGet(() -> {
            log.info("No active cart found for user {}, creating a new one.", user.getEmail());
            Cart newCart = Cart.builder()
                    .user(user)
                    .cartItems(new ArrayList<>())
                    .build();
            return cartRepository.save(newCart);
        });

        //4. Cross-store Guard: match the canteen, if matched add the quantity, else throw exception
        //if cart items exist, check whether the dish canteen matches current user's cart canteen
        if (!cart.getCartItems().isEmpty()) {
            Long currentCartCanteenId = cart.getCartItems().getFirst().getDish().getCanteen().getId();
            Long addedDishCanteenId = dish.getCanteen().getId();
            if (!currentCartCanteenId.equals(addedDishCanteenId)) {
                throw new BadRequestException("You can only add dishes from the same canteen.");
            }
        }
        //5. check whether the added dish exists, if exists then add the quantity, else add the dish to the cart
        Optional<CartItem> existingItem = cart.getCartItems().stream()
                .filter(item -> item.getDish().getId().equals(dish.getId()))
                .findFirst();

        if (existingItem.isPresent()) {
            CartItem item = existingItem.get();
            //add item quantity
            item.setQuantity(item.getQuantity() + quantity);
            item.setSubtotal(item.getPricePerUnit().multiply(BigDecimal.valueOf(item.getQuantity())));
            log.info("Updated existing CartItem, new quantity: {}", item.getQuantity());
        } else {
            //the item does not exist
            CartItem cartItem = CartItem.builder()
                    .cart(cart)
                    .dish(dish)
                    .quantity(quantity)
                    .pricePerUnit(dish.getPrice())
                    .subtotal(dish.getPrice().multiply(BigDecimal.valueOf(quantity)))
                    .build();
            cart.getCartItems().add(cartItem);
            log.info("Added new CartItem to cart.");
        }
        //6.persistence
        Cart savedCart = cartRepository.save(cart);
        CartDTO dto = cartMapper.toDTO(savedCart);
        enrichCartDTO(dto, savedCart);
        return dto;
    }

    @Override
    public CartDTO incrementItem(Long cartItemId) {
        log.info("Attempting to increment quantity for cartItem: {}", cartItemId);
        //Find user and cartItem, to match their cart
        CartItem item = getValidCartItemForCurrentUser(cartItemId);
        Cart cart = item.getCart();

        item.setQuantity(item.getQuantity() + 1);
        item.setSubtotal(item.getPricePerUnit().multiply(BigDecimal.valueOf(item.getQuantity())));

        Cart savedCart = cartRepository.save(cart);
        CartDTO dto = cartMapper.toDTO(savedCart);
        enrichCartDTO(dto, savedCart);
        return dto;
    }

    @Override
    public CartDTO decrementItem(Long cartItemId) {
        log.info("Attempting to decrement quantity for cartItem: {}", cartItemId);

        CartItem item = getValidCartItemForCurrentUser(cartItemId);
        Cart cart = item.getCart();

        if (item.getQuantity() > 1) {
            item.setQuantity(item.getQuantity() - 1);
            item.setSubtotal(item.getPricePerUnit().multiply(BigDecimal.valueOf(item.getQuantity())));

        } else {
            cart.getCartItems().remove(item);
        }
        Cart savedCart = cartRepository.save(cart);
        CartDTO dto = cartMapper.toDTO(savedCart);
        enrichCartDTO(dto, savedCart);
        return dto;
    }

    @Override
    public void removeCartItem(Long cartItemId) {
        log.info("Attempting to remove from cart for the cartItem {}", cartItemId);

        CartItem item = getValidCartItemForCurrentUser(cartItemId);
        Cart cart = item.getCart();

        cart.getCartItems().remove(item);
        log.info("CartItem {} removed from the cart collection.", cartItemId);

        //Trigger JPA orphanRemoval
        cartRepository.save(cart);
    }

    @Override
    @Transactional(readOnly = true)
    public CartDTO getShoppingCart() {
        log.info("Attempting to get the shopping cart for the current user");
        User user = userService.getCurrentLoggedInUser();
        Optional<Cart> optionalCart = cartRepository.findByUserId(user.getId());
        if (optionalCart.isEmpty()) {
            log.info("User {} does not have an active cart. Returning empty cart DTO.", user.getId());
            return CartDTO.builder()
                    .userId(user.getId())
                    .totalPrice(BigDecimal.ZERO)
                    .totalQuantity(0)
                    .items(new ArrayList<>())
                    .build();
        }

        Cart cart = optionalCart.get();
        CartDTO dto = cartMapper.toDTO(cart);
        enrichCartDTO(dto, cart);
        return dto;
    }

    @Override
    public void clearCart() {
        log.info("Attempting to clear the shopping cart.");
        User user = userService.getCurrentLoggedInUser();
        cartRepository.findByUserId(user.getId()).ifPresent(cart -> {
            cart.getCartItems().clear();
            cartRepository.save(cart);
        });
    }


    //----helper methods----
    private CartItem getValidCartItemForCurrentUser(Long cartItemId) {
        User user = userService.getCurrentLoggedInUser();
        CartItem item = cartItemRepository.findById(cartItemId).orElseThrow(
                () -> new ResourceNotFoundException("CartItem", "id", cartItemId)
        );

        if (!item.getCart().getUser().getId().equals(user.getId())) {
            throw new BadRequestException("You do not have the permission to modify the cart item.");
        }
        return item;
    }


    private void enrichCartDTO(CartDTO dto, Cart savedCart) {
        //pruning
        if (savedCart.getCartItems() == null || savedCart.getCartItems().isEmpty()) {
            dto.setTotalPrice(BigDecimal.ZERO);
            dto.setTotalQuantity(0);
            return;
        }

        //calculate the total price
//        BigDecimal total = BigDecimal.ZERO;
//        for(CartItem item: savedCart.getCartItems()){
//            total=total.add(item.getSubtotal());
//        }
        BigDecimal total = savedCart.getCartItems().stream()
//                .map(item->item.getSubtotal())
                .map(CartItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        //calculate the total quantity
        int quantity = savedCart.getCartItems().stream()
                .mapToInt(CartItem::getQuantity)
                .sum();

        dto.setTotalPrice(total);
        dto.setTotalQuantity(quantity);

        Dish dish = savedCart.getCartItems().getFirst().getDish();
        if (dish != null && dish.getCanteen() != null) {
            dto.setCanteenId(dish.getCanteen().getId());
            dto.setCanteenName(dish.getCanteen().getName());
        }
    }

}
