package fpt.legendcoffee.service.serviceImpl;

import fpt.legendcoffee.dto.app.WithdrawalRequestDTO;
import fpt.legendcoffee.entity.User;
import fpt.legendcoffee.entity.Wallet;
import fpt.legendcoffee.entity.WalletTransaction;
import fpt.legendcoffee.entity.WithdrawalRequest;
import fpt.legendcoffee.entity.enumeration.WithdrawalStatus;
import fpt.legendcoffee.repository.UserRepository;
import fpt.legendcoffee.repository.WalletRepository;
import fpt.legendcoffee.repository.WalletTransactionRepository;
import fpt.legendcoffee.repository.WithdrawalRequestRepository;
import fpt.legendcoffee.service.WalletService;
import fpt.legendcoffee.service.WithdrawalService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class WithdrawalServiceImpl implements WithdrawalService {

        private final WithdrawalRequestRepository withdrawalRequestRepository;
        private final WalletRepository walletRepository;
        private final WalletTransactionRepository walletTransactionRepository;
        private final UserRepository userRepository;
        private final WalletService walletService; // Dùng để debitAdminWallet khi approve

        // =========================================================================
        // User: Tạo yêu cầu rút tiền
        // =========================================================================

        @Override
        @Transactional
        public WithdrawalRequest requestWithdrawal(Long userId, WithdrawalRequestDTO dto) {
                User user = userRepository.findById(userId)
                                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng"));

                // Ràng buộc: Không được có request PENDING đang chờ xử lý
                if (withdrawalRequestRepository.existsByUserIdAndStatus(userId, WithdrawalStatus.PENDING)) {
                        throw new IllegalStateException(
                                        "Bạn đang có một yêu cầu rút tiền chờ xử lý. Vui lòng đợi admin xét duyệt trước khi tạo yêu cầu mới.");
                }

                // Lấy ví với Pessimistic Lock để tránh race condition
                Wallet wallet = walletRepository.findByUserIdWithLock(userId)
                                .orElseThrow(() -> new IllegalStateException("Bạn chưa có ví trong hệ thống"));

                BigDecimal requestedAmount = dto.getAmount();

                // Ràng buộc: Số dư khả dụng phải đủ
                if (wallet.getAvailableAmount().compareTo(requestedAmount) < 0) {
                        throw new IllegalStateException(
                                        String.format("Số dư khả dụng không đủ. Khả dụng: %s ₫, Yêu cầu rút: %s ₫",
                                                        wallet.getAvailableAmount().toPlainString(),
                                                        requestedAmount.toPlainString()));
                }

                // Reserved pattern: trừ available, cộng reserved
                wallet.setAvailableAmount(wallet.getAvailableAmount().subtract(requestedAmount));
                wallet.setReservedAmount(wallet.getReservedAmount().add(requestedAmount));
                walletRepository.save(wallet);

                // Ghi log giao dịch
                saveTransaction(wallet, requestedAmount, "WITHDRAWAL_RESERVED",
                                "Tạm giữ để rút tiền: " + requestedAmount.toPlainString() + " ₫ → " + dto.getBankName()
                                                + " " + dto.getAccountNumber());

                // Tạo yêu cầu rút tiền
                WithdrawalRequest withdrawalRequest = WithdrawalRequest.builder()
                                .user(user)
                                .amount(requestedAmount)
                                .bankName(dto.getBankName())
                                .bankCode(dto.getBankCode())
                                .accountNumber(dto.getAccountNumber())
                                .accountHolder(dto.getAccountHolder())
                                .status(WithdrawalStatus.PENDING)
                                .build();

                WithdrawalRequest saved = withdrawalRequestRepository.save(withdrawalRequest);
                log.info("[Withdrawal] UserId={} tạo yêu cầu rút {} ₫ sang {} - {}",
                                userId, requestedAmount, dto.getBankName(), dto.getAccountNumber());

                return saved;
        }

        // =========================================================================
        // Admin: Chấp nhận yêu cầu rút tiền
        // =========================================================================

        @Override
        @Transactional
        public WithdrawalRequest approveWithdrawal(Long requestId) {
                WithdrawalRequest request = getAndValidatePending(requestId);

                Wallet wallet = walletRepository.findByUserIdWithLock(request.getUser().getId())
                                .orElseThrow(() -> new IllegalStateException("Không tìm thấy ví của user"));

                BigDecimal amount = request.getAmount();

                // Ràng buộc: reserved phải đủ (bình thường luôn đủ nếu luồng đúng)
                if (wallet.getReservedAmount().compareTo(amount) < 0) {
                        throw new IllegalStateException("Lỗi hệ thống: Số dư reserved không đủ để approve");
                }

                // 1. Trừ reserved của USER (tiền rời ví user)
                wallet.setReservedAmount(wallet.getReservedAmount().subtract(amount));
                walletRepository.save(wallet);
                saveTransaction(wallet, amount, "WITHDRAWAL_COMPLETED",
                                "Rút tiền thành công: " + amount.toPlainString() + " ₫ → " + request.getBankName() + " "
                                                + request.getAccountNumber());

                // 2. Trừ ví ADMIN (admin thực sự chuyển tiền ra ngân hàng của user)
                String adminDesc = "Chuyển tiền rút cho UserId=" + request.getUser().getId()
                                + " (#" + requestId + ") → " + request.getBankName() + " " + request.getAccountNumber();
                walletService.debitAdminWallet(amount, adminDesc);

                // Cập nhật trạng thái request
                request.setStatus(WithdrawalStatus.APPROVED);
                request.setProcessedAt(LocalDateTime.now());

                WithdrawalRequest saved = withdrawalRequestRepository.save(request);
                log.info("[Withdrawal] Admin APPROVE requestId={}, UserId={}, amount={}",
                                requestId, request.getUser().getId(), amount);

                return saved;
        }

        // =========================================================================
        // Admin: Từ chối yêu cầu rút tiền
        // =========================================================================

        @Override
        @Transactional
        public WithdrawalRequest rejectWithdrawal(Long requestId, String reason) {
                if (reason == null || reason.isBlank()) {
                        throw new IllegalArgumentException("Vui lòng cung cấp lý do từ chối");
                }

                WithdrawalRequest request = getAndValidatePending(requestId);

                Wallet wallet = walletRepository.findByUserIdWithLock(request.getUser().getId())
                                .orElseThrow(() -> new IllegalStateException("Không tìm thấy ví của user"));

                BigDecimal amount = request.getAmount();

                // Hoàn tiền: trừ reserved, cộng lại available
                wallet.setReservedAmount(wallet.getReservedAmount().subtract(amount));
                wallet.setAvailableAmount(wallet.getAvailableAmount().add(amount));
                walletRepository.save(wallet);

                saveTransaction(wallet, amount, "WITHDRAWAL_REJECTED",
                                "Hoàn tiền do yêu cầu rút bị từ chối. Lý do: " + reason);

                // Cập nhật trạng thái request
                request.setStatus(WithdrawalStatus.REJECTED);
                request.setRejectReason(reason);
                request.setProcessedAt(LocalDateTime.now());

                WithdrawalRequest saved = withdrawalRequestRepository.save(request);
                log.info("[Withdrawal] Admin REJECT requestId={}, UserId={}, amount={}, reason={}",
                                requestId, request.getUser().getId(), amount, reason);

                return saved;
        }

        // =========================================================================
        // Query
        // =========================================================================

        @Override
        @Transactional(readOnly = true)
        public List<WithdrawalRequest> getRequestsByUser(Long userId) {
                return withdrawalRequestRepository.findByUserIdOrderByCreatedAtDesc(userId);
        }

        @Override
        @Transactional(readOnly = true)
        public List<WithdrawalRequest> getAllRequests() {
                return withdrawalRequestRepository.findAllWithUserOrderByCreatedAtDesc();
        }

        // =========================================================================
        // Helper
        // =========================================================================

        private WithdrawalRequest getAndValidatePending(Long requestId) {
                WithdrawalRequest request = withdrawalRequestRepository.findById(requestId)
                                .orElseThrow(() -> new IllegalArgumentException(
                                                "Không tìm thấy yêu cầu rút tiền #" + requestId));

                if (request.getStatus() != WithdrawalStatus.PENDING) {
                        throw new IllegalStateException(
                                        "Yêu cầu #" + requestId + " đã được xử lý trước đó (trạng thái: "
                                                        + request.getStatus() + ")");
                }
                return request;
        }

        private void saveTransaction(Wallet wallet, BigDecimal amount, String type, String description) {
                WalletTransaction tx = WalletTransaction.builder()
                                .wallet(wallet)
                                .amount(amount)
                                .transactionType(type)
                                .description(description)
                                .build();
                walletTransactionRepository.save(tx);
        }
}
