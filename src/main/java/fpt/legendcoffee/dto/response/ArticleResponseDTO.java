package fpt.legendcoffee.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ArticleResponseDTO {
    private Long id;
    private String title;
    private String authorName; // Chỉ lấy tên tác giả thay vì cả Object User
    private String summary;
    private String publishedDate; // Đã format dd/MM/yyyy
    private String status;
    private String coverImageUrl;
}
