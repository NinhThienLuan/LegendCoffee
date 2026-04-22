package fpt.legendcoffee.service.serviceImpl;

import java.math.BigDecimal;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fpt.legendcoffee.entity.Order;
import fpt.legendcoffee.entity.RefundRequest;
import fpt.legendcoffee.entity.Wallet;
import fpt.legendcoffee.entity.WalletTransaction;
import fpt.legendcoffee.entity.enumeration.OrderStatus;
import fpt.legendcoffee.entity.enumeration.RefundStatus;
import fpt.legendcoffee.repository.OrderRepository;
import fpt.legendcoffee.repository.RefundRequestRepository;
import fpt.legendcoffee.repository.WalletRepository;
import fpt.legendcoffee.repository.WalletTransactionRepository;
import fpt.legendcoffee.service.RefundService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class RefundServiceImpl implements RefundService {

    private final RefundRequestRepository refundRequestRepository;
    private final WalletRepository walletRepository;
    private final WalletTransactionRepository walletTransactionRepository;
    private final OrderRepository orderRepository;

    // ==========================================
    // USER GỬI YÊU CẦU HOÀN TIỀN
    // ==========================================
    @Override
    @Transactional
    public RefundRequest createRefundRequest(Long userId, Long orderId, BigDecimal amount, String reason) {
        log.info("[Refund] User {} yêu cầu hoàn tiền cho Order {}", userId, orderId);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đơn hàng"));

        if (!order.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("Bạn không có quyền yêu cầu hoàn tiền cho đơn hàng này");
        }

        if (order.getStatus() != OrderStatus.CONFIRMED && order.getStatus() != OrderStatus.SHIPPING && order.getStatus() != OrderStatus.COMPLETED) {
            throw new IllegalStateException("Chỉ đơn hàng đã thanh toán mới được phép hoàn tiền");
        }

        if (refundRequestRepository.existsByOrderIdAndStatus(orderId, RefundStatus.PENDING)) {
            throw new IllegalStateException("Đơn hàng này đang có yêu cầu hoàn tiền chờ xử lý");
        }

        // Tạo yêu cầu mới
        RefundRequest request = RefundRequest.builder()
                .user(order.getUser())
                .order(order)
                .amount(amount)
                .status(RefundStatus.PENDING)
                .build();

        // Tạm khóa đơn hàng
        order.setStatus(OrderStatus.REFUND_PROCESSING);
        orderRepository.save(order);

        return refundRequestRepository.save(request);
    }

    // ==========================================
    // ADMIN XÁC NHẬN HOÀN TIỀN
    // ==========================================
    @Override
    @Transactional
    public void approveRefund(Long refundRequestId) {
        log.info("[Refund] Bắt đầu duyệt hoàn tiền cho Request ID: {}", refundRequestId);

        RefundRequest request = refundRequestRepository.findById(refundRequestId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy yêu cầu hoàn tiền"));

        if (request.getStatus() != RefundStatus.PENDING) {
            throw new IllegalStateException("Yêu cầu này đã được xử lý trước đó");
        }

        // Khóa và cập nhật ví
        Wallet wallet = walletRepository.findByUserIdWithLock(request.getUser().getId())
                .orElseThrow(() -> new IllegalStateException("Hệ thống chưa tạo ví cho User này"));

        BigDecimal refundAmount = request.getAmount();
        wallet.setAmount(wallet.getAmount().add(refundAmount));
        walletRepository.save(wallet);

        // Lưu lịch sử giao dịch ví
        WalletTransaction transaction = WalletTransaction.builder()
                .wallet(wallet)
                .amount(refundAmount)
                .transactionType("REFUND")
                .description("Hoàn tiền cho đơn hàng #" + request.getOrder().getId())
                .build();
        walletTransactionRepository.save(transaction);

        // Đổi trạng thái
        request.setStatus(RefundStatus.APPROVED);
        refundRequestRepository.save(request);

        Order order = request.getOrder();
        order.setStatus(OrderStatus.REFUNDED);
        Wallet adminWallet = walletRepository.findByIdWithLock(1L)
        .orElseThrow(() -> new IllegalStateException("Không tìm thấy ví Admin/Hệ thống để trừ tiền"));

// Đảm bảo ví admin không bị âm (tuỳ policy của bạn, có thể bỏ qua nếu cho phép ví hệ thống âm)
if (adminWallet.getAmount().compareTo(refundAmount) < 0) {
    throw new IllegalStateException("Số dư ví hệ thống không đủ để hoàn tiền");
}

adminWallet.setAmount(adminWallet.getAmount().subtract(refundAmount));
walletRepository.save(adminWallet);
        orderRepository.save(order);

        log.info("[Refund] Đã duyệt thành công Request {}. Đã cộng {} vào Wallet {}", 
                 refundRequestId, refundAmount, wallet.getId());
        }

    // ==========================================
    // ADMIN TỪ CHỐI HOÀN TIỀN
    // ==========================================
    @Override
    @Transactional
    public void rejectRefund(Long refundRequestId) {
        log.info("[Refund] Admin từ chối hoàn tiền cho Request ID: {}", refundRequestId);

        RefundRequest request = refundRequestRepository.findById(refundRequestId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy yêu cầu hoàn tiền"));

        if (request.getStatus() != RefundStatus.PENDING) {
            throw new IllegalStateException("Yêu cầu này đã được xử lý trước đó");
        }

        // Đổi trạng thái request
        request.setStatus(RefundStatus.REJECTED);
        refundRequestRepository.save(request);

        // Khôi phục trạng thái đơn hàng (để user dùng tiếp hoặc khiếu nại lại)
        Order order = request.getOrder();
        order.setStatus(OrderStatus.COMPLETED); 
        orderRepository.save(order);
    }
}