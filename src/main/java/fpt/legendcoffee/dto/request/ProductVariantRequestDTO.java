package fpt.legendcoffee.dto.request;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductVariantRequestDTO {

    /** Tên biến thể (ví dụ: Robusta 60kg Xay mịn). */
    private String variantName;

    /** Hình thức đóng gói (ví dụ: Túi, Bao, Lon). */
    private String packaging;

    /** Kích cỡ / khối lượng (đơn vị: gram). */
    private Integer size;

    /** Đơn giá bán của biến thể. */
    private BigDecimal price;

    /** Số lượng tồn kho. */
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
