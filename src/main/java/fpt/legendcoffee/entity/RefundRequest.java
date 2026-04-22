package fpt.legendcoffee.entity;
import java.math.BigDecimal;

import fpt.legendcoffee.common.infrastructure.BaseEntity;
import fpt.legendcoffee.entity.enumeration.RefundStatus;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Entity
@SuperBuilder
@Table(name = "refund_requests")

public class RefundRequest extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Order order; // Để biết hoàn tiền cho đơn hàng nào

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user; // Để biết tiền sẽ được trả về cho ai

    private BigDecimal amount; // Số tiền hoàn
    
    @Enumerated(EnumType.STRING)
    private RefundStatus status; // PENDING, APPROVED, REJECTED
}