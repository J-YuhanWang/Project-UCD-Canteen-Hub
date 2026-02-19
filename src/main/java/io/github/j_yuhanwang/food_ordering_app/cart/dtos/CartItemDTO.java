package io.github.j_yuhanwang.food_ordering_app.cart.dtos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * @author YuhanWang
 * @Date 02/02/2026 7:18 pm
 */

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CartItemDTO {
    private Long id;

    private Long menuId;
    private String menuName;
    private String menuImageUrl;

    private Integer quantity;

    private BigDecimal pricePerUnit;

    private BigDecimal subtotal;

    //The service layer will calculate this value based on Menu.isAvailableNow().
    private boolean isAvailable;

}
