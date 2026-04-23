package fpt.legendcoffee.dto.request;

import java.math.BigDecimal;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductVariantRequestDTO {

    /** Tên biến thể (ví dụ: Robusta 60kg Xay mịn). */
    @NotBlank(message = "Tên biến thể không được để trống")
    private String variantName;

    /** Hình thức đóng gói (ví dụ: Túi, Bao, Lon). */
    private String packaging;

    /** Kích cỡ / khối lượng (đơn vị: gram). */
    private Integer size;

    /** Đơn giá bán của biến thể. */
    @NotNull(message = "Giá không được để trống")
    @Min(value = 1000, message = "Giá không được nhỏ hơn 1.000")
    private BigDecimal price;

    /** Số lượng tồn kho. */
    @NotNull(message = "Số lượng tồn kho không được để trống")
    @Min(value = 0, message = "Số lượng không được nhỏ hơn 0")
    private Integer stockQuantity;

    /** Trạng thái kích hoạt biến thể. */
    private Boolean isActive;

    public String getVariantName() {
        return variantName;
    }

    public void setVariantName(String variantName) {
        this.variantName = variantName;
    }

    public String getPackaging() {
        return packaging;
    }

    public void setPackaging(String packaging) {
        this.packaging = packaging;
    }

    public Integer getSize() {
        return size;
    }

    public void setSize(Integer size) {
        this.size = size;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public Integer getStockQuantity() {
        return stockQuantity;
    }

    public void setStockQuantity(Integer stockQuantity) {
        this.stockQuantity = stockQuantity;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }
}
