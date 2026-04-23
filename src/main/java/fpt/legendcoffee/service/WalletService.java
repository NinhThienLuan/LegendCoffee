package fpt.legendcoffee.service;

import fpt.legendcoffee.entity.Wallet;

import java.math.BigDecimal;

public interface WalletService {
    void creditWallet(Long userId, BigDecimal amount, String description);
    void debitWallet(Long userId, BigDecimal amount, String description);
    void creditAdminWallet(BigDecimal amount, String description);
    void debitAdminWallet(BigDecimal amount, String description);

    /** Lấy thông tin ví theo userId (availableAmount + reservedAmount) */
    Wallet getWallet(Long userId);
}
