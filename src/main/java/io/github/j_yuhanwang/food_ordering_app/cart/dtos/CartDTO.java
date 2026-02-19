package io.github.j_yuhanwang.food_ordering_app.cart.dtos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author YuhanWang
 * @Date 02/02/2026 6:55 pm
 */

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CartDTO {
    private Long id;

    private Long userId;

    private Long canteenId;
    private String canteenName;

    //This field is not in the database; it is calculated by
    // the Service layer by adding up the List<CartItem>
    private BigDecimal totalPrice;

    private Integer totalQuantity;

    private List<CartItemDTO> items;

}
