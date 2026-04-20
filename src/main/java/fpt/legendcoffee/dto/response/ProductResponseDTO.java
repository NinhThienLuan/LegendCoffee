package fpt.legendcoffee.dto.response;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductResponseDTO {
    private Long id;
    private String name;
    private String categoryName;
    private BigDecimal price;
    private Integer stockQuantity;
    private Integer soldCount;
    private Boolean isActive;
    private String imageUrl;
}
