package io.github.j_yuhanwang.food_ordering_app.payment.mapper;

import io.github.j_yuhanwang.food_ordering_app.payment.dtos.PaymentDTO;
import io.github.j_yuhanwang.food_ordering_app.payment.entity.Payment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

/**
 * @author YuhanWang
 * @Date 10/04/2026 7:47 pm
 */
@Mapper(componentModel = "spring",unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PaymentMapper {

    @Mapping(source = "user.id", target = "userId")
    @Mapping(source = "user.name",target = "userName")
    @Mapping(source = "order.id",target = "orderId")
    @Mapping(source = "order.canteen.name",target="canteenName")
    @Mapping(target="isSuccess",expression = "java(payment.getPaymentStatus()==PaymentStatus.COMPLETED)")
    PaymentDTO toDTO(Payment entity);
}
