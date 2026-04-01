package io.github.j_yuhanwang.food_ordering_app.cart.mapper;

import io.github.j_yuhanwang.food_ordering_app.cart.dtos.CartItemDTO;
import io.github.j_yuhanwang.food_ordering_app.cart.entity.CartItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

/**
 * @author YuhanWang
 * @Date 27/03/2026 4:33 pm
 */
@Mapper(componentModel = "spring")
public interface CartItemMapper {

    @Mapping(source = "dish.id",target = "dishId")
    @Mapping(source = "dish.name",target = "dishName")
    @Mapping(source = "dish.imageUrl",target = "dishImageUrl")
    @Mapping(source = "dish.available",target = "isAvailable")
    CartItemDTO toDTO(CartItem cartItem);

    /**
     * Maps a list of entities to a list of DTOs.
     * MapStruct automatically reuses toDTO() logic for each element in the list.
     */
    List<CartItemDTO> toDtoList(List<CartItem> cartItems);

}
