package fpt.legendcoffee.entity;

import fpt.legendcoffee.common.infrastructure.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "product_variants")
public class ProductVariant extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;

    @Column(name = "variant_name", columnDefinition = "NVARCHAR(255)")
    private String variantName;

    @Column(name = "packaging", columnDefinition = "NVARCHAR(255)")
    private String packaging;

    @Column(name = "size")
    private Integer size;

    @Column(name = "price", precision = 18)
    private BigDecimal price;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "stock_quantity")
    private Integer stockQuantity;

    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;

}