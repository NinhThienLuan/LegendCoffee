package fpt.legendcoffee.entity;

import fpt.legendcoffee.common.infrastructure.BaseEntity;
import fpt.legendcoffee.entity.enumeration.PaymentMethod;
import fpt.legendcoffee.entity.enumeration.PaymentStatus;
import fpt.legendcoffee.entity.enumeration.PaymentType;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "payments")
public class Payment extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Order order;

    @Column(name = "amount", precision = 18)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private PaymentStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_type")
    private PaymentType paymentType;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method")
    private PaymentMethod paymentMethod;

    @Column(name = "payment_date")
    private LocalDateTime paymentDate;

    @Column(name = "trans_ref")
    private String transRef;

    @Column(name = "error_code")
    private String errorCode;

    @Column(name = "error_message", columnDefinition = "NVARCHAR(255)")
    private String errorMessage;

}