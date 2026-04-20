package fpt.legendcoffee.controller;

import fpt.legendcoffee.dto.ghn.WebhookPayloadDTO;
import fpt.legendcoffee.service.ShippingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Endpoint nhận webhook từ GHN.
 *
 * Đăng ký URL này trong GHN Dashboard:
 *   GHN Dashboard → Cài đặt → Webhook → Thêm URL
 *   URL: https://yourdomain.com/webhook/ghn
 *
 * GHN sẽ POST JSON vào đây mỗi khi trạng thái đơn thay đổi.
 * Luôn trả về 200 OK để GHN biết đã nhận thành công (tránh retry).
 *
 * Lưu ý: Route /webhook/ghn phải được thêm vào PUBLIC_MATCHERS
 *         trong SecurityConstants để không yêu cầu đăng nhập.
 */
@Slf4j
@RestController
@RequestMapping("/webhook")
@RequiredArgsConstructor
public class GHNWebhookController {

    private final ShippingService shippingService;

    /**
     * POST /webhook/ghn
     * GHN sẽ gọi endpoint này khi trạng thái đơn hàng thay đổi.
     */
    @PostMapping("/ghn")
    public ResponseEntity<Map<String, String>> handleGHNWebhook(
            @RequestBody WebhookPayloadDTO payload) {

        log.info("[Webhook] Received from GHN: orderCode={}, status={}",
                payload.getOrderCode(), payload.getStatus());

        try {
            shippingService.handleWebhook(payload.getOrderCode(), payload.getStatus());
            return ResponseEntity.ok(Map.of("status", "received"));
        } catch (Exception e) {
            // Vẫn trả 200 để GHN không retry liên tục, log lỗi để xử lý sau
            log.error("[Webhook] Error processing GHN webhook: {}", e.getMessage(), e);
            return ResponseEntity.ok(Map.of("status", "received_with_error"));
        }
    }
}
