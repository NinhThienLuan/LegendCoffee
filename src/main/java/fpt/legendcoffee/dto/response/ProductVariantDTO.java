package fpt.legendcoffee.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProductVariantDTO {
    private Long id;

    private Long productId;      // 🔥 không trả cả Product
    private String productName;

    private String variantName;
    private String packaging;
    private Integer size;

    private BigDecimal price;

    private String imageUrl;
    private Integer stockQuantity;

    private Boolean isActive;
}
