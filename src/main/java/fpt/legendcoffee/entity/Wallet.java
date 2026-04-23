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
@Table(name = "wallets")
public class Wallet extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    /**
     * Số dư khả dụng — user có thể rút hoặc dùng để thanh toán.
     * Tương đương với cột `amount` cũ (migration: đổi tên cột thành available_amount).
     */
    @Column(name = "available_amount", precision = 18, nullable = false)
    @Builder.Default
    private BigDecimal availableAmount = BigDecimal.ZERO;

    /**
     * Số tiền đang bị khóa (reserved) — đang chờ admin duyệt yêu cầu rút tiền.
     * Tổng số dư thực tế = availableAmount + reservedAmount.
     */
    @Column(name = "reserved_amount", precision = 18, nullable = false)
    @Builder.Default
    private BigDecimal reservedAmount = BigDecimal.ZERO;

}