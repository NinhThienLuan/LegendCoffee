package fpt.legendcoffee.entity;

import fpt.legendcoffee.common.infrastructure.BaseEntity;
import fpt.legendcoffee.entity.enumeration.ArticleStatus;
import jakarta.persistence.*;
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

    @Column(name = "title", columnDefinition = "NVARCHAR(255)")
    private String title;

    @Column(name = "content_json", columnDefinition = "NVARCHAR(MAX)")
    private String contentJson;

    @Column(name = "cover_image_url")
    private String coverImageUrl;

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