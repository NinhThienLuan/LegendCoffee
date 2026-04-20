package fpt.legendcoffee.dto.app;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CheckoutItemRequestDTO {
    private Long variantId;
    private Long comboId;
    private Integer quantity;
}

