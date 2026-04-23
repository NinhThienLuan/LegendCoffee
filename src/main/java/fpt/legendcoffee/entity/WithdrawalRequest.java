package fpt.legendcoffee.entity;

import fpt.legendcoffee.common.infrastructure.BaseEntity;
import fpt.legendcoffee.entity.enumeration.WithdrawalStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Yêu cầu rút tiền từ Ví của user ra tài khoản ngân hàng.
 *
 * Luồng Reserved Pattern:
 * - PENDING  : availableAmount -= amount, reservedAmount += amount
 * - APPROVED : reservedAmount -= amount (tiền rời hệ thống)
 * - REJECTED : reservedAmount -= amount, availableAmount += amount (hoàn lại)
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "withdrawal_requests")
public class WithdrawalRequest extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /** Số tiền muốn rút (VNĐ) */
    @Column(name = "amount", precision = 18, nullable = false)
    private BigDecimal amount;

    /** Tên ngân hàng (ví dụ: Vietcombank) */
    @Column(name = "bank_name", nullable = false, length = 100)
    private String bankName;

    /** Mã ngân hàng BIN (ví dụ: VCB) */
    @Column(name = "bank_code", nullable = false, length = 20)
    private String bankCode;

    /** Số tài khoản thụ hưởng */
    @Column(name = "account_number", nullable = false, length = 50)
    private String accountNumber;

    /** Tên chủ tài khoản thụ hưởng */
    @Column(name = "account_holder", nullable = false, length = 100)
    private String accountHolder;

    /** Trạng thái xét duyệt */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private WithdrawalStatus status = WithdrawalStatus.PENDING;

    /** Lý do từ chối (chỉ có khi REJECTED) */
    @Column(name = "reject_reason", length = 500)
    private String rejectReason;

    /** Thời điểm admin xử lý (approve/reject) */
    @Column(name = "processed_at")
    private LocalDateTime processedAt;
}
