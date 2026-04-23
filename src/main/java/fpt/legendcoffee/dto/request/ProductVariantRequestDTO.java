package fpt.legendcoffee.dto.request;

import java.math.BigDecimal;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductVariantRequestDTO {

    /** ID của biến thể (dùng khi cập nhật). */
    private Long id;

    /** Tên biến thể (ví dụ: Robusta 60kg Xay mịn). */
    @NotBlank(message = "Tên biến thể không được để trống")
    @Size(min = 10, message = "Tên biến thể phải có ít nhất 10 ký tự")
    private String variantName;

    /** Hình thức đóng gói (ví dụ: Túi, Bao, Lon). */
    @Size(min = 3, message = "Đóng gói phải có ít nhất 3 ký tự")
    private String packaging;

    /** Kích cỡ / khối lượng (đơn vị: gram). */
    @Min(value = 250, message = "Kích cỡ thấp nhất là 250")
    private Integer size;

    /** Đơn giá bán của biến thể. */
    @NotNull(message = "Giá không được để trống")
    @Min(value = 10000, message = "Giá không được nhỏ hơn 10.000")
    private BigDecimal price;

    /** Số lượng tồn kho. */
    @NotNull(message = "Số lượng tồn kho không được để trống")
    @Min(value = 0, message = "Số lượng không được nhỏ hơn 0")
    private Integer stockQuantity;

    /** Trạng thái kích hoạt biến thể. */
    private Boolean isActive;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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
