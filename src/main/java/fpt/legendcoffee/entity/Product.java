package fpt.legendcoffee.entity;

import java.time.LocalDate;

import fpt.legendcoffee.common.infrastructure.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Entity
@Table(name = "products")
public class Product extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    @Column(name = "name", columnDefinition = "NVARCHAR(255)")
    private String name;

    @Column(name = "description", columnDefinition = "NVARCHAR(MAX)")
    private String description;

    @Column(name = "origin", columnDefinition = "NVARCHAR(255)")
    private String origin;

    @Column(name = "expiry_date")
    private LocalDate expiryDate;

    @Column(name = "manufacturer_date")
    private LocalDate manufacturerDate;

    @Column(name = "image_url", columnDefinition = "NVARCHAR(1000)")
    private String imageUrl;

    @Column(name = "image_public_id", columnDefinition = "NVARCHAR(255)")
    private String imagePublicId;

    @Column(name = "is_active")
    @lombok.Builder.Default
    private Boolean isActive = true;

}