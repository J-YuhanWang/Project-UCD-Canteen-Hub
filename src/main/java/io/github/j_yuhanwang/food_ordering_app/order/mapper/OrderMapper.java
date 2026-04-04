package io.github.j_yuhanwang.food_ordering_app.order.mapper;

import io.github.j_yuhanwang.food_ordering_app.order.dtos.OrderDTO;
import io.github.j_yuhanwang.food_ordering_app.order.entity.Order;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * @author YuhanWang
 * @Date 04/04/2026 3:26 pm
 */
@Mapper(componentModel = "spring",uses={OrderItemMapper.class})
public interface OrderMapper {
    @Mapping(source = "user.id", target = "userId")
    @Mapping(source = "user.name", target = "userName")
    @Mapping(source = "canteen.id", target = "canteenId")
    @Mapping(source = "canteen.name", target = "canteenName")
    @Mapping(source = "orderItems",target = "items")
    OrderDTO toDTO(Order entity);
}
