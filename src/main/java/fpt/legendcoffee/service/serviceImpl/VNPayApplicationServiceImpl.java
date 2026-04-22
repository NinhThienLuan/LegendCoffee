package fpt.legendcoffee.service.serviceImpl;

import fpt.legendcoffee.entity.Order;
import fpt.legendcoffee.entity.Payment;
import fpt.legendcoffee.entity.enumeration.OrderStatus;
import fpt.legendcoffee.entity.enumeration.PaymentMethod;
import fpt.legendcoffee.entity.enumeration.PaymentStatus;
import fpt.legendcoffee.dto.request.PaymentReturnDTO;
import fpt.legendcoffee.dto.response.VNPayIpnResponseDTO;
import fpt.legendcoffee.service.ShippingService;
import fpt.legendcoffee.service.VNPayApplicationService;
import fpt.legendcoffee.service.VNPayService;
import fpt.legendcoffee.repository.OrderRepository;
import fpt.legendcoffee.repository.PaymentRepository;
import fpt.legendcoffee.service.WalletService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

/**
 * Business logic VNPay payment.
 * Nguyên tắc phân chia trách nhiệm:
 * Tạo Payment PENDING + sinh URL VNPay. (@Transactional)
 * Chỉ validate chữ ký + trả DTO hiển thị. (Không update DB)
 */
@Service
public class VNPayApplicationServiceImpl implements VNPayApplicationService {

    private static final Logger log = LoggerFactory.getLogger(VNPayApplicationServiceImpl.class);

    private final VNPayService vnPayService;
    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;
    private final ShippingService shippingService;
    private final WalletService walletService;

    public VNPayApplicationServiceImpl(VNPayService vnPayService,
            OrderRepository orderRepository,
            PaymentRepository paymentRepository,
            ShippingService shippingService,
            WalletService walletService) {
        this.vnPayService = vnPayService;
        this.orderRepository = orderRepository;
        this.paymentRepository = paymentRepository;
        this.shippingService = shippingService;
        this.walletService = walletService;
    }

    // createPayment — Tạo Payment PENDING + URL VNPay
    @Override
    @Transactional
    public String createPayment(Long orderId, String ipAddress) {
        // 1. Load order — ném lỗi nếu không tồn tại
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Không tìm thấy đơn hàng với ID: " + orderId));

        BigDecimal totalAmount = order.getTotalAmount();
        if (totalAmount == null || totalAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalStateException("Tổng tiền đơn hàng không hợp lệ: " + orderId);
        }

        // 2. Tạo txnRef duy nhất: orderId + timestamp, TUI ĐANG NGHĨ LÀM V ĐỂ TRÁNH BỊ
        // TRÙNG
        String txnRef = orderId + "_" + System.currentTimeMillis();

        // 3. Tạo bản ghi Payment trạng thái PENDING và lưu DB
        Payment payment = Payment.builder()
                .order(order)
                .amount(totalAmount)
                .status(PaymentStatus.PENDING)
                .paymentMethod(PaymentMethod.VNPAY)
                .paymentDate(LocalDateTime.now())
                .transRef(txnRef)
                .build();
        paymentRepository.save(payment);

        // 4. Sinh mô tả đơn hàng
        String orderInfo = "Thanh toan don hang #" + orderId;

        // 5. Tạo URL VNPay và trả về
        String paymentUrl = vnPayService.createPaymentUrl(
                totalAmount.longValue(), txnRef, orderInfo, ipAddress);

        log.info("[VNPay] Tạo payment thành công - OrderId={}, TxnRef={}", orderId, txnRef);
        return paymentUrl;
    }

    // =========================================================================
    // handleReturn — CHỈ validate chữ ký, trả DTO hiển thị, KHÔNG update DB
    // =========================================================================

    @Override
    public PaymentReturnDTO handleReturn(Map<String, String> queryParams) {
        // Validate chữ ký để tránh giả mạo
        if (!vnPayService.validateSignature(queryParams)) {
            log.warn("[VNPay Return] Chữ ký không hợp lệ");
            return PaymentReturnDTO.failure("Chữ ký không hợp lệ", null, null);
        }

        String responseCode = get(queryParams, "vnp_ResponseCode");
        String txnRef = getOrDefault(queryParams, "vnp_TxnRef", "");
        boolean isSuccess = "00".equals(responseCode);

        if (isSuccess) {
            log.info("[VNPay Return] Hiển thị thành công - TxnRef={}", txnRef);
            return PaymentReturnDTO.success(
                    txnRef,
                    parseAmount(get(queryParams, "vnp_Amount")),
                    get(queryParams, "vnp_TransactionNo"),
                    get(queryParams, "vnp_BankCode"),
                    responseCode,
                    get(queryParams, "vnp_PayDate"));
        }

        log.warn("[VNPay Return] Hiển thị thất bại - ResponseCode={}, TxnRef={}", responseCode, txnRef);
        return PaymentReturnDTO.failure(resolveErrorMessage(responseCode), responseCode, txnRef);
    }

    // =========================================================================
    // handleIpn — NGUỒN CHÂN LÝ DUY NHẤT cập nhật DB
    // =========================================================================

    @Override
    @Transactional
    public VNPayIpnResponseDTO handleIpn(Map<String, String> queryParams) {
        String txnRef = getOrDefault(queryParams, "vnp_TxnRef", "N/A");

        // Validate chữ ký
        if (!vnPayService.validateSignature(queryParams)) {
            log.warn("[VNPay IPN] Chữ ký không hợp lệ - TxnRef={}", txnRef);
            return VNPayIpnResponseDTO.invalidSignature();
        }

        // Tìm Payment theo txnRef — IPN sẽ không biết paymentId
        Optional<Payment> paymentOpt = paymentRepository.findByTransRef(txnRef);
        if (paymentOpt.isEmpty()) {
            log.warn("[VNPay IPN] Không tìm thấy payment - TxnRef={}", txnRef);
            return VNPayIpnResponseDTO.orderNotFound();
        }

        Payment payment = paymentOpt.get();

        // Idempotency check — tránh xử lý trùng khi VNPay retry IPN
        if (payment.getStatus() != PaymentStatus.PENDING) {
            log.info("[VNPay IPN] Đã xử lý trước đó, bỏ qua - TxnRef={}, Status={}",
                    txnRef, payment.getStatus());
            return VNPayIpnResponseDTO.confirmSuccess(); // Vẫn trả 00 để VNPay không gửi lại
        }

        // Kiểm tra số tiền (tránh gian lận)
        BigDecimal receivedAmount = parseAmount(get(queryParams, "vnp_Amount"));
        if (payment.getAmount().compareTo(receivedAmount) != 0) {
            log.error("[VNPay IPN] Số tiền không khớp - Expected={}, Received={}, TxnRef={}",
                    payment.getAmount(), receivedAmount, txnRef);
            return VNPayIpnResponseDTO.invalidAmount();
        }

        // Xác định kết quả giao dịch
        String responseCode = get(queryParams, "vnp_ResponseCode");
        String transactionStatus = get(queryParams, "vnp_TransactionStatus");
        boolean isSuccess = "00".equals(responseCode) && "00".equals(transactionStatus);

        // Cập nhật Payment
        payment.setStatus(isSuccess ? PaymentStatus.SUCCESS : PaymentStatus.FAILED);
        payment.setPaymentDate(LocalDateTime.now());
        if (!isSuccess) {
            payment.setErrorCode(responseCode);
            payment.setErrorMessage(resolveErrorMessage(responseCode));
        }
        paymentRepository.save(payment);

        // Nếu thành công → cập nhật Order status và đẩy sang GHN
        if (isSuccess) {
            Order order = payment.getOrder();
            order.setStatus(OrderStatus.CONFIRMED);
            orderRepository.save(order);

            // Đẩy đơn sang GHN
            try {
                shippingService.pushOrderToGHN(order.getId());
                log.info("[VNPay IPN] Thanh toán thành công & Đã đẩy đơn sang GHN - TxnRef={}, OrderId={}",
                        txnRef, order.getId());
            } catch (Exception e) {
                log.error("[VNPay IPN] Thanh toán thành công nhưng lỗi khi đẩy sang GHN: {}", e.getMessage());
                // Tuỳ business: có thể cho phép admin push tay sau nếu lỗi ở đây
            }

            // Cộng tiền vào ví admin
            try {
                String description = "Nhận tiền từ đơn hàng #" + order.getId();
                walletService.creditAdminWallet(receivedAmount, description);
            } catch (Exception e) {
                log.error("[VNPay IPN] Lỗi khi cộng tiền vào ví admin: {}", e.getMessage());
            }
        } else {
            log.warn("[VNPay IPN] Thanh toán thất bại - TxnRef={}, ResponseCode={}",
                    txnRef, responseCode);
        }

        // Luôn trả "00" để VNPay biết đã nhận IPN (dù thành công hay thất bại)
        return VNPayIpnResponseDTO.confirmSuccess();
    }

    // Helper methods

    private String get(Map<String, String> params, String key) {
        String value = params.get(key);
        return (value != null && !value.isEmpty()) ? value : null;
    }

    private String getOrDefault(Map<String, String> params, String key, String defaultValue) {
        String value = get(params, key);
        return value != null ? value : defaultValue;
    }

    /**
     * Parse số tiền từ VNPay (đơn vị x100) sang BigDecimal (VND thực).
     * Ví dụ: "5000000" → 50000.00 VND
     */
    private BigDecimal parseAmount(String rawAmount) {
        if (rawAmount == null || rawAmount.isEmpty()) {
            return BigDecimal.ZERO;
        }
        try {
            return new BigDecimal(rawAmount).divide(BigDecimal.valueOf(100));
        } catch (NumberFormatException e) {
            log.warn("[VNPay] Không parse được vnp_Amount: {}", rawAmount);
            return BigDecimal.ZERO;
        }
    }

    /**
     * Chuyển VNPay response code sang thông báo lỗi tiếng Việt.
     */
    private String resolveErrorMessage(String responseCode) {
        if (responseCode == null)
            return "Lỗi không xác định";
        return switch (responseCode) {
            case "07" -> "Trừ tiền thành công. Giao dịch bị nghi ngờ (liên quan tới lừa đảo)";
            case "09" -> "Thẻ/Tài khoản chưa đăng ký dịch vụ InternetBanking";
            case "10" -> "Xác thực thông tin thẻ/tài khoản quá 3 lần";
            case "11" -> "Đã hết hạn chờ thanh toán";
            case "12" -> "Thẻ/Tài khoản bị khóa";
            case "13" -> "Sai mật khẩu xác thực giao dịch (OTP)";
            case "24" -> "Khách hàng hủy giao dịch";
            case "51" -> "Tài khoản không đủ số dư";
            case "65" -> "Tài khoản đã vượt hạn mức giao dịch trong ngày";
            case "75" -> "Ngân hàng thanh toán đang bảo trì";
            case "79" -> "Nhập sai mật khẩu thanh toán quá số lần quy định";
            default -> "Thanh toán thất bại (mã lỗi: " + responseCode + ")";
        };
    }
}
