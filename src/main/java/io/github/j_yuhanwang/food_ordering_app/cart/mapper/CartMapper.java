package io.github.j_yuhanwang.food_ordering_app.cart.mapper;

import io.github.j_yuhanwang.food_ordering_app.cart.dtos.CartDTO;
import io.github.j_yuhanwang.food_ordering_app.cart.entity.Cart;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import javax.swing.*;

/**
 * @author YuhanWang
 * @Date 27/03/2026 3:13 pm
 */
@Mapper(componentModel="spring",uses= {CartItemMapper.class})
public interface CartMapper {
    @Mapping(source = "user.id", target = "userId")
    @Mapping(source = "cartItems",target="items")
    @Mapping(target = "canteenId",ignore = true)
    @Mapping(target = "canteenName",ignore = true)
    @Mapping(target = "totalPrice",ignore = true)
    @Mapping(target = "totalQuantity",ignore = true)
    CartDTO toDTO(Cart entity);

}
