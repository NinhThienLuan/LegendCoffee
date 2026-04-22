package fpt.legendcoffee.entity;

import fpt.legendcoffee.common.infrastructure.BaseEntity;
import fpt.legendcoffee.entity.enumeration.OrderStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "orders")
public class Order extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "voucher_id")
    private Voucher voucher;

    @Column(name = "order_date")
    private LocalDateTime orderDate;

    @Column(name = "sub_total", precision = 18)
    private BigDecimal subTotal;

    @Column(name = "discount", precision = 18)
    private BigDecimal discount;

    @Column(name = "total_amount", precision = 18)
    private BigDecimal totalAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private OrderStatus status;

    @OneToOne(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private ShippingInfo shippingInfo;

    @OneToMany(mappedBy = "order")
    private List<OrderItem> orderItems;
}