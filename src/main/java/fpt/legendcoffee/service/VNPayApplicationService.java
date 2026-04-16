package fpt.legendcoffee.service;

import fpt.legendcoffee.dto.response.VNPayIpnResponseDTO;
import fpt.legendcoffee.dto.request.PaymentReturnDTO;

import java.util.Map;

public interface VNPayApplicationService {

    /**
     * Tạo URL thanh toán VNPay cho một đơn hàng.
     * Đồng thời tạo bản ghi Payment (PENDING) trong DB.
     */
    String createPayment(Long orderId, String ipAddress);

    /**
     * Xử lý Return URL — VNPay redirect người dùng về sau khi thanh toán.
     * Tầng này CHỈ validate chữ ký và trả DTO hiển thị.
     */
    PaymentReturnDTO handleReturn(Map<String, String> queryParams);

    /**
     * Xử lý IPN (Instant Payment Notification) — VNPay server gọi trực tiếp.
     * Đây là nơi DUY NHẤT cập nhật trạng thái Payment và Order trong DB.
     * Có idempotency check để tránh xử lý trùng khi VNPay retry.
     */
    VNPayIpnResponseDTO handleIpn(Map<String, String> queryParams);
}
