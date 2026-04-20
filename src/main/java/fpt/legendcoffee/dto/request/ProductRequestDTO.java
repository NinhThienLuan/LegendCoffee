package fpt.legendcoffee.dto.request;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductRequestDTO {

    /** Tên sản phẩm hiển thị trên hệ thống. */
    private String name;

    /** Mô tả chi tiết về sản phẩm. */
    private String description;

    /** Nguồn gốc/xuất xứ của sản phẩm. */
    private String origin;

    /** Ngày hết hạn của sản phẩm. */
    private LocalDate expiryDate;

    /** Ngày sản xuất của sản phẩm. */
    private LocalDate manufacturerDate;

    /** ID danh mục, dùng để liên kết Category khi lưu Product. */
    private Long categoryId;

    /** Trạng thái kích hoạt sản phẩm (true: hoạt động, false: ẩn). */
    private Boolean isActive;

    /** File ảnh upload từ form thêm/sửa sản phẩm. */
    private MultipartFile image;

    /** Danh sách biến thể gửi từ form. */
    private List<ProductVariantRequestDTO> variants = new ArrayList<>();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getOrigin() {
        return origin;
    }

    public void setOrigin(String origin) {
        this.origin = origin;
    }

    public LocalDate getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(LocalDate expiryDate) {
        this.expiryDate = expiryDate;
    }

    public LocalDate getManufacturerDate() {
        return manufacturerDate;
    }

    public void setManufacturerDate(LocalDate manufacturerDate) {
        this.manufacturerDate = manufacturerDate;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public MultipartFile getImage() {
        return image;
    }

    public void setImage(MultipartFile image) {
        this.image = image;
    }

    public List<ProductVariantRequestDTO> getVariants() {
        return variants;
    }

    public void setVariants(List<ProductVariantRequestDTO> variants) {
        this.variants = variants;
    }

    // Hàm tiện ích để kiểm tra logic
    public boolean isValidDateRange() {
        if (manufacturerDate == null || expiryDate == null)
            return false;
        return expiryDate.isAfter(manufacturerDate);
    }
}