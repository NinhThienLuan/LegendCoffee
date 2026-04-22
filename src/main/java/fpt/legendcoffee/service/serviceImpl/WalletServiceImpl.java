package fpt.legendcoffee.service.serviceImpl;

<<<<<<< Updated upstream
=======
import fpt.legendcoffee.common.util.SecurityUtils;
import fpt.legendcoffee.dto.request.WithDrawRequestDTO;
import fpt.legendcoffee.entity.User;
import fpt.legendcoffee.entity.Wallet;
import fpt.legendcoffee.entity.WalletTransaction;
import fpt.legendcoffee.entity.enumeration.TransactionStatus;
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
import java.time.LocalDateTime;
>>>>>>> Stashed changes
import java.util.List;

import org.springframework.stereotype.Service;

import fpt.legendcoffee.entity.Wallet;
import fpt.legendcoffee.repository.WalletRepository;
import fpt.legendcoffee.service.WalletService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class WalletServiceImpl implements WalletService {
    private final WalletRepository walletRepository;

<<<<<<< Updated upstream
    @Override
    public List<Wallet> getAllWallets() {
        return walletRepository.findAll();
=======
    // Thêm đoạn này vào bên trong WalletServiceImpl

    @Override
    @Transactional
    public void creditAdminWallet(BigDecimal amount, String description) {
        User admin = findAdmin();
        // Gọi hàm creditWallet đã có sẵn để nạp tiền cho Admin
        creditWallet(admin.getId(), amount, description);
    }

    @Override
    @Transactional
    public void debitAdminWallet(BigDecimal amount, String description) {
        User admin = findAdmin();
        // Gọi hàm debitWallet đã có sẵn để trừ tiền của Admin
        debitWallet(admin.getId(), amount, description);
    }
    // --- CÁC HÀM NGHIỆP VỤ (CÓ GIAO DỊCH & LOCK) ---

    @Override
    @Transactional
    public void requestWithdrawal(WithDrawRequestDTO request) {
        Long userId = SecurityUtils.getCurrentUserId();
        if (userId == null)
            throw new IllegalStateException("Vui lòng đăng nhập");

        // 1. Khóa ví để đảm bảo không ai đụng vào khi đang tính toán
        Wallet wallet = walletRepository.findByUserIdWithLock(userId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy ví"));

        // 2. Kiểm tra số dư
        if (wallet.getBalance().compareTo(request.getAmount()) < 0) {
            throw new IllegalStateException("Số dư ví không đủ để rút số tiền này");
        }

        // 3. Trừ tiền ngay lập tức (Tạm giữ)
        wallet.setBalance(wallet.getBalance().subtract(request.getAmount()));
        walletRepository.save(wallet);

        // 4. Lưu giao dịch PENDING
        String description = "Rút tiền về " + request.getBankCode() + " - STK: " + request.getAccountNumber();
        saveTransaction(wallet, request.getAmount(), "WITHDRAW", description, TransactionStatus.PENDING);

        log.info("[Withdraw] User {} rút {}. Còn lại: {}", userId, request.getAmount(), wallet.getBalance());
    }

    @Override
    @Transactional
    public void creditWallet(Long userId, BigDecimal amount, String description) {
        // Dùng Lock khi nạp tiền để tránh sai số dư nếu có nhiều luồng nạp cùng lúc
        Wallet wallet = walletRepository.findByUserIdWithLock(userId)
                .orElseGet(() -> createNewWallet(userId));

        wallet.setBalance(wallet.getBalance().add(amount));
        walletRepository.save(wallet);

        saveTransaction(wallet, amount, "INCOME", description, TransactionStatus.SUCCESS);
    }

    @Override
    @Transactional
    public void debitWallet(Long userId, BigDecimal amount, String description) {
        Wallet wallet = walletRepository.findByUserIdWithLock(userId)
                .orElseThrow(() -> new IllegalArgumentException("Ví không tồn tại"));

        if (wallet.getBalance().compareTo(amount) < 0) {
            throw new IllegalStateException("Số dư không đủ");
        }

        wallet.setBalance(wallet.getBalance().subtract(amount));
        walletRepository.save(wallet);

        saveTransaction(wallet, amount, "EXPENSE", description, TransactionStatus.SUCCESS);
    }

    // --- CÁC HÀM TRUY VẤN (KHÔNG DÙNG LOCK ĐỂ TRÁNH LỖI VÀ TĂNG TỐC) ---

    @Override
    @Transactional
    public BigDecimal getWalletBalance(Long userId) {
        return walletRepository.findByUserId(userId)
                .map(Wallet::getBalance)
                .orElse(BigDecimal.ZERO);
    }

    @Override
    @Transactional
    public Wallet getWalletByUserId(Long userId) {
        return walletRepository.findByUserId(userId)
                .orElseGet(() -> createNewWallet(userId));
    }

    @Override
    @Transactional
    public List<WalletTransaction> getTransactionsByUserId(Long userId) {
        // 1. Xác định ID cuối cùng cần dùng
        Long targetUserId = (userId == null) ? SecurityUtils.getCurrentUserId() : userId;

        if (targetUserId == null)
            return List.of();

        // 2. Sử dụng targetUserId (biến này không bị thay đổi nên Lambda sẽ nhận)
        Wallet wallet = walletRepository.findByUserId(targetUserId)
                .orElseGet(() -> createNewWallet(targetUserId));

        return walletTransactionRepository.findByWalletOrderByCreatedAtDesc(wallet);
    }

    @Override
    @Transactional
    public Wallet getAdminWallet() {
        User admin = findAdmin();
        return walletRepository.findByUserId(admin.getId())
                .orElseGet(() -> createNewWallet(admin.getId()));
    }

    // --- HÀM HỖ TRỢ (PRIVATE) ---

    private Wallet createNewWallet(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User không tồn tại"));
        Wallet newWallet = Wallet.builder()
                .user(user)
                .balance(BigDecimal.ZERO)
                .build();
        return walletRepository.save(newWallet);
    }

    private User findAdmin() {
        return userRepository.findByRole(UserRole.ADMIN).stream().findFirst()
                .orElseThrow(() -> new IllegalStateException("Không tìm thấy ADMIN"));
    }

    private void saveTransaction(Wallet wallet, BigDecimal amount, String type, String description,
            TransactionStatus status) {
        WalletTransaction transaction = WalletTransaction.builder()
                .wallet(wallet)
                .amount(amount)
                .transactionType(type)
                .description(description)
                .status(status)
                .createdAt(LocalDateTime.now()) // Đảm bảo có ngày tạo để sắp xếp
                .build();
        walletTransactionRepository.save(transaction);
>>>>>>> Stashed changes
    }
}