package fpt.legendcoffee.common.mapper;


import fpt.legendcoffee.dto.request.ArticleRequestDTO;
import fpt.legendcoffee.dto.response.ArticleResponseDTO;
import fpt.legendcoffee.entity.Article;
import java.time.format.DateTimeFormatter;

public class ArticleMapper {
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public static ArticleResponseDTO toResponseDTO(Article entity) {
        return ArticleResponseDTO.builder()
                .id(entity.getId())
                .title(entity.getTitle())
                .summary(entity.getSummary())
                .authorName(entity.getUser() != null ? entity.getUser().getUsername() : "Ẩn danh")
                .publishedDate(entity.getPublishedAt() != null ? entity.getPublishedAt().format(formatter) : "--")
                .status(entity.getStatus().toString())
                .coverImageUrl(entity.getCoverImageUrl())
                .build();
    }

    // Trong file ArticleMapper.java

    // Chuyển từ Entity sang RequestDTO để hiển thị lên Form khi Sửa
    public static ArticleRequestDTO toRequestDTO(Article entity) {
        ArticleRequestDTO dto = new ArticleRequestDTO();
        dto.setId(entity.getId());
        dto.setTitle(entity.getTitle());
        dto.setSummary(entity.getSummary());
        dto.setCoverImageUrl(entity.getCoverImageUrl());
        dto.setContentJson(entity.getContentJson());
        dto.setStatus(entity.getStatus());
        return dto;
    }

    // Cập nhật các trường từ DTO vào Entity (tránh gán đè các trường metadata như createdAt)
    public static void updateEntity(Article entity, ArticleRequestDTO dto) {
        entity.setTitle(dto.getTitle());
        entity.setSummary(dto.getSummary());
        entity.setContentJson(dto.getContentJson());
        entity.setCoverImageUrl(dto.getCoverImageUrl());
        entity.setStatus(dto.getStatus());
    }

}

