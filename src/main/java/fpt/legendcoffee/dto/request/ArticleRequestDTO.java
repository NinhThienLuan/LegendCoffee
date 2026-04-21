package fpt.legendcoffee.dto.request;

import fpt.legendcoffee.entity.enumeration.ArticleStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ArticleRequestDTO {
    private Long id; // Dùng cho trường hợp Update
    @NotBlank(message = "Tiêu đề bài viết không được để trống")
    @Size(min = 10, max = 200, message = "Tiêu đề phải từ 10 đến 200 ký tự")
    private String title;
    @NotBlank(message = "Tóm tắt không được để trống")
    @Size(max = 500, message = "Tóm tắt không được vượt quá 500 ký tự")
    private String summary;
    private String coverImageUrl;
    @NotBlank(message = "Nội dung bài viết không được để trống")
    private String contentJson;
    @NotNull(message = "Vui lòng chọn trạng thái")
    private ArticleStatus status;
}
