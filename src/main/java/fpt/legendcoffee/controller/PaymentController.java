package fpt.legendcoffee.controller;

import fpt.legendcoffee.common.util.WebUtils;
import fpt.legendcoffee.dto.request.PaymentReturnDTO;
import fpt.legendcoffee.dto.response.VNPayIpnResponseDTO;
import fpt.legendcoffee.service.VNPayApplicationService;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

/**
 *
 * <pre>
 * [User] → POST /payment/vnpay/create?orderId=X
 *        → redirect sang VNPay gateway
 *        → [VNPay] GET /payment/vnpay/callback (verify kết quả ở phía người dùng)
 *        → redirect /payment/result  (kết quả thanh toán)
 *        → [VNPay Server] GET /payment/vnpay/ipn (veriyfy ở phía server payment call to hệ thống mình) → update DB
 * </pre>
 */
@Controller
public class PaymentController {

    private static final Logger log = LoggerFactory.getLogger(PaymentController.class);

    private final VNPayApplicationService vnPayApplicationService;

    public PaymentController(VNPayApplicationService vnPayApplicationService) {
        this.vnPayApplicationService = vnPayApplicationService;
    }

    // =========================================================================
    // GET /payment — Trang payment hiện tại (giữ nguyên route cũ)
    // =========================================================================

    @GetMapping("/payment")
    public String paymentPage(Model model) {
        return "payment/payment";
    }

    // POST /payment/vnpay/create — Khởi tạo thanh toán VNPay
    /**
     * Nhận orderId, tạo Payment PENDING, sinh URL VNPay và redirect sang đó.
     * User cần đăng nhập để gọi endpoint này
     */
    @PostMapping("/payment/vnpay/create")
    public String createVNPayPayment(@RequestParam Long orderId,
            HttpServletRequest request) {
        try {
            String ipAddress = WebUtils.getClientIp(request);
            String paymentUrl = vnPayApplicationService.createPayment(orderId, ipAddress);
            log.info("[Payment] Redirect sang VNPay - OrderId={}", orderId);
            return "redirect:" + paymentUrl;
        } catch (IllegalArgumentException e) {
            log.warn("[Payment] Lỗi tạo payment - {}", e.getMessage());
            return "redirect:/payment/result?error=order_not_found";
        } catch (Exception e) {
            log.error("[Payment] Lỗi không xác định khi tạo payment", e);
            return "redirect:/payment/result?error=internal_error";
        }
    }

    // ================== ENDPOINT TEST TẠM THỜI (MỞ BẰNG TRÌNH DUYỆT) ==================
    @GetMapping("/test/vnpay")
    @ResponseBody
    public String testVNPayPayment() {
        return """
               <html>
               <body onload="document.forms[0].submit()">
                 <form method="POST" action="/payment/vnpay/create?orderId=1">
                    Đang chuyển hướng sang VNPay...
                 </form>
               </body>
               </html>
               """;
    }
    // ==================================================================================

    // GET /payment/vnpay/callback — VNPay redirect người dùng về đây
    /**
     * VNPay redirect người dùng về sau khi thanh toán.
     * CHỈ validate chữ ký và hiển thị kết quả — KHÔNG cập nhật DB.
     */
    @GetMapping("/payment/vnpay/callback")
    public String handleVNPayCallback(HttpServletRequest request, Model model) {
        PaymentReturnDTO result = vnPayApplicationService.handleReturn(
                WebUtils.extractQueryParams(request));
        model.addAttribute("payment", result);
        return "payment/payment";
    }

    // GET /payment/vnpay/ipn — VNPay server gọi để xác nhận giao dịch

    /**
     * IPN (Instant Payment Notification): VNPay server gọi server-to-server.
     * Đây là nơi DUY NHẤT cập nhật trạng thái Payment và Order trong DB.
     */
    @GetMapping("/payment/vnpay/ipn")
    @ResponseBody
    public VNPayIpnResponseDTO handleVNPayIpn(HttpServletRequest request) {
        return vnPayApplicationService.handleIpn(WebUtils.extractQueryParams(request));
    }

    // GET /payment/result — Trang kết quả (fallback / direct access)
    @GetMapping("/payment/result")
    public String paymentResult(@RequestParam(required = false) String error, Model model) {
        if (error != null) {
            model.addAttribute("payment",
                    PaymentReturnDTO.failure(resolveErrorParam(error), null, null));
        }
        return "payment/payment";
    }

    // Helper — Presentation logic của riêng controller này
    /** Map error query param → thông báo tiếng Việt để hiển thị cho user. */
    private String resolveErrorParam(String error) {
        return switch (error) {
            case "order_not_found" -> "Không tìm thấy đơn hàng";
            case "internal_error" -> "Lỗi hệ thống, vui lòng thử lại";
            default -> "Đã có lỗi xảy ra";
        };
    }
}
