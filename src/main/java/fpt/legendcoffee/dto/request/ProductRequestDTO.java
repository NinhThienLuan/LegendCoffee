package fpt.legendcoffee.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductRequestDTO {

    private String name;

    private String description;

    private String origin;

    private String expiryDate;

    private String manufacturerDate;

    private Long categoryId; // Dùng ID để tìm Category trong Service

    private Boolean isActive;

    // Thuộc tính ảnh gộp vào DTO
    private MultipartFile image;
}