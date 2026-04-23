package fpt.legendcoffee.dto.response;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ArticleResponseDTO {
    private Long id;
    @NotNull(message = "Title không thể để trống")
    @NotBlank(message = "Title không thể chỉ chứa khoảng trắng")
    private String title;


    private String authorName; // Chỉ lấy tên tác giả thay vì cả Object User
    private String summary;
    private String publishedDate; // Đã format dd/MM/yyyy
    private String status;
    private String coverImageUrl;
}
