package fpt.legendcoffee.entity;

import fpt.legendcoffee.common.infrastructure.BaseEntity;
import fpt.legendcoffee.entity.enumeration.ArticleStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "articles")
public class Article extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @NotBlank(message = "Tiêu đề bài viết không được để trống")
    @Size(min = 10, max = 200, message = "Tiêu đề phải từ 10 đến 200 ký tự")
    @Column(name = "title", columnDefinition = "NVARCHAR(255)")
    private String title;

    @NotBlank(message = "Nội dung bài viết không được để trống")
    @Column(name = "content_json", columnDefinition = "NVARCHAR(MAX)")
    private String contentJson;

    @Column(name = "cover_image_url")
    private String coverImageUrl;

    @NotBlank(message = "Tóm tắt không được để trống")
    @Size(max = 255, message = "Tóm tắt không được vượt quá 255 ký tự")
    @Column(name = "summary", columnDefinition = "NVARCHAR(255)")
    private String summary;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private ArticleStatus status;

    @Column(name = "published_at")
    private LocalDateTime publishedAt;

    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;

}