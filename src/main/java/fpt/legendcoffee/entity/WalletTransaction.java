package fpt.legendcoffee.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import fpt.legendcoffee.common.infrastructure.BaseEntity;
import fpt.legendcoffee.entity.enumeration.TransactionStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Entity
@Table(name = "wallet_transactions")
public class WalletTransaction extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "wallet_id", nullable = false)
    private Wallet wallet;

    @Column(name = "amount", precision = 18, nullable = false)
    private BigDecimal amount;

    @Column(name = "transaction_type", length = 50)
    private String transactionType; // Ví dụ: WITHDRAW, DEPOSIT, REFUND

    @Column(name = "description", length = 255)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20)
    private TransactionStatus status;

    @Column(name = "note", length = 500)
    private String note; // Lý do từ chối hoặc ghi chú thêm

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "processed_by")
    private User processedBy; // Admin xử lý

    @Column(name = "processed_at")
    private LocalDateTime processedAt;
}