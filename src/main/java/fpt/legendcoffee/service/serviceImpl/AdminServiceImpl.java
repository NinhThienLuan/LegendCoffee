package fpt.legendcoffee.service.serviceImpl;

import fpt.legendcoffee.entity.Wallet;
import fpt.legendcoffee.entity.WalletTransaction;
import fpt.legendcoffee.entity.enumeration.TransactionStatus;
import fpt.legendcoffee.repository.WalletRepository;
import fpt.legendcoffee.repository.WalletTransactionRepository;
import fpt.legendcoffee.common.util.SecurityUtils;
import fpt.legendcoffee.service.AdminService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private final WalletTransactionRepository transactionRepository;
    private final WalletRepository walletRepository;

    @Override
    @Transactional
    public void approveWithdrawal(Long transactionId) {
        // 1. Tìm giao dịch
        WalletTransaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy giao dịch với ID: " + transactionId));

        // 2. Kiểm tra trạng thái phải là PENDING
        if (transaction.getStatus() != TransactionStatus.PENDING) {
            throw new IllegalStateException("Giao dịch này không ở trạng thái chờ duyệt");
        }

        // 3. Chuyển trạng thái sang SUCCESS
        transaction.setStatus(TransactionStatus.SUCCESS);
        transaction.setProcessedAt(LocalDateTime.now());
        transaction.setProcessedBy(SecurityUtils.getCurrentUser()); 

        transactionRepository.save(transaction);
        log.info("[Admin] Approved withdrawal transaction ID: {}", transactionId);
    }

    @Override
    @Transactional
    public void rejectWithdrawal(Long transactionId, String note) {
        // 1. Tìm giao dịch
        WalletTransaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy giao dịch với ID: " + transactionId));

        // 2. Kiểm tra trạng thái phải là PENDING
        if (transaction.getStatus() != TransactionStatus.PENDING) {
            throw new IllegalStateException("Giao dịch này không ở trạng thái chờ duyệt");
        }

        // 3. Thực hiện HOÀN TIỀN (Cộng lại tiền vào ví người dùng)
        Wallet wallet = transaction.getWallet();
        // Khóa ví để đảm bảo an toàn khi cộng tiền
        Wallet lockedWallet = walletRepository.findByUserIdWithLock(wallet.getUser().getId())
                .orElseThrow(() -> new IllegalStateException("Không tìm thấy ví để hoàn tiền"));
        
        lockedWallet.setBalance(lockedWallet.getBalance().add(transaction.getAmount()));
        walletRepository.save(lockedWallet);

        // 4. Chuyển trạng thái sang FAILED và lưu ghi chú
        transaction.setStatus(TransactionStatus.FAILED);
        transaction.setNote(note);
        transaction.setProcessedAt(LocalDateTime.now());
        transaction.setProcessedBy(SecurityUtils.getCurrentUser());
        
        transactionRepository.save(transaction);

        // 5. Tạo một bản ghi giao dịch REFUND để dễ theo dõi (tùy chọn)
        log.info("[Admin] Rejected withdrawal transaction ID: {}. Reason: {}. Refunded: {}", 
                transactionId, note, transaction.getAmount());
    }

    @Override
    public List<WalletTransaction> getPendingWithdrawals() {
        return transactionRepository.findByStatus(TransactionStatus.PENDING);
    }
}
