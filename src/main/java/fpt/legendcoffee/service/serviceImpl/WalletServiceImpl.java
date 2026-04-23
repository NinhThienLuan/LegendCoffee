package fpt.legendcoffee.service.serviceImpl;

import fpt.legendcoffee.entity.User;
import fpt.legendcoffee.entity.Wallet;
import fpt.legendcoffee.entity.WalletTransaction;
import fpt.legendcoffee.entity.enumeration.UserRole;
import fpt.legendcoffee.repository.UserRepository;
import fpt.legendcoffee.repository.WalletRepository;
import fpt.legendcoffee.repository.WalletTransactionRepository;
import fpt.legendcoffee.service.WalletService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class WalletServiceImpl implements WalletService {

    private final WalletRepository walletRepository;
    private final WalletTransactionRepository walletTransactionRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public void creditWallet(Long userId, BigDecimal amount, String description) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng với ID: " + userId));
        
        Wallet wallet = getOrCreateWallet(user);

        wallet.setAmount(wallet.getAmount().add(amount));
        walletRepository.save(wallet);

        saveTransaction(wallet, amount, "INCOME", description);
        log.info("[Wallet] Credited {} to UserId={} ({}). Reason: {}", amount, userId, user.getUsername(), description);
    }

    @Override
    @Transactional
    public void debitWallet(Long userId, BigDecimal amount, String description) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng với ID: " + userId));
        
        Wallet wallet = getOrCreateWallet(user);

        if (wallet.getAmount().compareTo(amount) < 0) {
            throw new IllegalStateException("Số dư ví không đủ");
        }
        wallet.setAmount(wallet.getAmount().subtract(amount));
        walletRepository.save(wallet);

        saveTransaction(wallet, amount, "EXPENSE", description);
        log.info("[Wallet] Debited {} from UserId={} ({}). Reason: {}", amount, userId, user.getUsername(), description);
    }

    @Override
    @Transactional
    public void creditAdminWallet(BigDecimal amount, String description) {
        User admin = findAdmin();
        creditWallet(admin.getId(), amount, description);
    }

    @Override
    @Transactional
    public void debitAdminWallet(BigDecimal amount, String description) {
        User admin = findAdmin();
        debitWallet(admin.getId(), amount, description);
    }

    private User findAdmin() {
        List<User> admins = userRepository.findByRole(UserRole.ADMIN);
        if (admins.isEmpty()) {
            throw new IllegalStateException("Không tìm thấy tài khoản ADMIN trong hệ thống");
        }
        return admins.get(0);
    }

    private Wallet getOrCreateWallet(User user) {
        return walletRepository.findByUserIdWithLock(user.getId())
                .orElseGet(() -> {
                    Wallet newWallet = Wallet.builder()
                            .user(user)
                            .amount(BigDecimal.ZERO)
                            .build();
                    return walletRepository.save(newWallet);
                });
    }

    private void saveTransaction(Wallet wallet, BigDecimal amount, String type, String description) {
        WalletTransaction transaction = WalletTransaction.builder()
                .wallet(wallet)
                .amount(amount)
                .transactionType(type)
                .description(description)
                .build();
        walletTransactionRepository.save(transaction);
    }
}
