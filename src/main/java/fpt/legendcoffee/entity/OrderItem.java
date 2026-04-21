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
@Table(name = "order_items")
public class OrderItem extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "variant_id")
    private ProductVariant variant;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "combo_id")
    private Combo combo;

    @Column(name = "quantity")
    private Integer quantity;

    @Column(name = "unit_price", precision = 18)
    private BigDecimal unitPrice;

    @Column(name = "sub_total", precision = 18)
    private BigDecimal subTotal;

    @Column(name = "discount", precision = 18)
    private BigDecimal discount;

    @Column(name = "total_amount", precision = 18)
    private BigDecimal totalAmount;

    @Column(name = "status")
    private String status;

    @PrePersist
    @PreUpdate
    private void validateTarget() {
        boolean hasVariant = variant != null;
        boolean hasCombo = combo != null;

        if (hasVariant == hasCombo) {
            throw new IllegalStateException("OrderItem phải tham chiếu đúng một trong hai: variant hoặc combo");
        }
    }
}