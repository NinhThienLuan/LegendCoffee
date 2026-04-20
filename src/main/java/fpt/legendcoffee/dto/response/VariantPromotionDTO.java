package fpt.legendcoffee.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class VariantPromotionDTO {
    private Long id;
    private Long promotionId;
    private Long variantId;
}
