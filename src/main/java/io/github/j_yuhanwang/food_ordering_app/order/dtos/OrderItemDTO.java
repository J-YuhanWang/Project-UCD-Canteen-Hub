package io.github.j_yuhanwang.food_ordering_app.order.dtos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * @author YuhanWang
 * @Date 02/02/2026 5:57 pm
 */
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OrderItemDTO {
    private Long id;

    //Flattened Dish Info
    private Long dishId;
    private String dishName;
    private String dishImageUrl;

    //Match Entity Naming
    private BigDecimal pricePerUnit;
    private Integer quantity;
    private BigDecimal subtotal;
}
