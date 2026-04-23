package fpt.legendcoffee.service;
import java.math.BigDecimal;
import java.util.List;

import fpt.legendcoffee.dto.request.WithDrawRequestDTO;
import fpt.legendcoffee.entity.Wallet;
import fpt.legendcoffee.entity.WalletTransaction;

public interface WalletService {
    List<Wallet> getAllWallets();
    void creditWallet(Long userId, BigDecimal amount, String description);
    void debitWallet(Long userId, BigDecimal amount, String description);
    void creditAdminWallet(BigDecimal amount, String description);
    void debitAdminWallet(BigDecimal amount, String description);
    void requestWithdrawal(WithDrawRequestDTO request);
    Wallet getAdminWallet();
    List<WalletTransaction> getTransactionsByUserId(Long userId);
    BigDecimal getWalletBalance(Long userId);
    Wallet getWalletByUserId(Long userId);
}
