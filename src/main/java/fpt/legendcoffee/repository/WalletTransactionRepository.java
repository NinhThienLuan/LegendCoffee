package fpt.legendcoffee.repository;

import fpt.legendcoffee.entity.WalletTransaction;
import fpt.legendcoffee.entity.enumeration.TransactionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface WalletTransactionRepository extends JpaRepository<WalletTransaction, Long> {
    List<WalletTransaction> findByStatus(TransactionStatus status);
    List<WalletTransaction> findByWalletOrderByCreatedAtDesc(fpt.legendcoffee.entity.Wallet wallet);
}