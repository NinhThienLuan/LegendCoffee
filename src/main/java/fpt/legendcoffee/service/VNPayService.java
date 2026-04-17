package fpt.legendcoffee.service;

import java.util.Map;

/**
 * Tạo URL thanh toán và xác thực chữ ký.
 */
public interface VNPayService {

    /**
     * Tạo URL thanh toán VNPay.
     * amount Số tiền (VND)
     * txnRef Mã giao dịch (duy nhất) — thường là orderId hoặc paymentId
     * orderInfo Thông tin mô tả đơn hàng
     * ipAddress IP của người dùng
     * URL thanh toán VNPay đầy đủ (kèm vnp_SecureHash)
     */
    String createPaymentUrl(long amount, String txnRef, String orderInfo, String ipAddress);

    /**
     * Xác thực chữ ký HMAC-SHA512 từ VNPay callback/IPN.
     * queryParams Các tham số từ query string (tất cả vnp_ params)
     * true nếu chữ ký hợp lệ (dữ liệu không bị giả mạo)
     */
    boolean validateSignature(Map<String, String> queryParams);
}
