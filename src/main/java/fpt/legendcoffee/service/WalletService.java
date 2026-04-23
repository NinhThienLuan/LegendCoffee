package fpt.legendcoffee.service;

import java.math.BigDecimal;

public interface WalletService {
    void creditWallet(Long userId, BigDecimal amount, String description);
    void debitWallet(Long userId, BigDecimal amount, String description);
    void creditAdminWallet(BigDecimal amount, String description);
    void debitAdminWallet(BigDecimal amount, String description);
}
