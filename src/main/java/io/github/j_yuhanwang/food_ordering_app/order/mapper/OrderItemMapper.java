package io.github.j_yuhanwang.food_ordering_app.order.mapper;

import io.github.j_yuhanwang.food_ordering_app.order.dtos.OrderItemDTO;
import io.github.j_yuhanwang.food_ordering_app.order.entity.OrderItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

/**
 * @author YuhanWang
 * @Date 04/04/2026 3:46 pm
 */
@Mapper(componentModel = "spring")
public interface OrderItemMapper {

    @Mapping(source = "dish.id",target = "dishId")
    OrderItemDTO toDTO(OrderItem entity);

    List<OrderItemDTO> toDtoList(List<OrderItem> entities);
}
