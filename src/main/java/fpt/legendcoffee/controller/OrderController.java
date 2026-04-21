package fpt.legendcoffee.controller;

import fpt.legendcoffee.dto.app.CheckoutRequestDTO;
import fpt.legendcoffee.entity.Order;
import fpt.legendcoffee.entity.ShippingInfo;
import fpt.legendcoffee.repository.UserRepository;
import fpt.legendcoffee.service.OrderService;
import fpt.legendcoffee.service.ShippingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

@Slf4j
@Controller
@RequiredArgsConstructor
public class OrderController {

    private final ShippingService shippingService;
    private final OrderService orderService;
    private final UserRepository userRepository;

    // =========================================================================
    // Trang danh sách đơn hàng & chi tiết
    // =========================================================================

    @GetMapping("/orders")
    public String orderPage(Model model) {
        return "order/order";
    }

    @GetMapping("/order-detail")
    public String orderDetailPage(Model model) {
        return "order/orderDetail";
    }

    // =========================================================================
    // Giỏ hàng
    // =========================================================================

    @GetMapping("/cart")
    public String cartPage(Model model) {
        return "cart/cart";
    }

    // =========================================================================
    // Checkout — hiển thị form + xử lý đặt hàng
    // =========================================================================

    /**
     * GET /checkout
     * Hiển thị trang checkout với danh sách tỉnh/thành phố.
     * Quận/huyện và phường/xã được tải động qua AJAX (/api/shipping/districts & /api/shipping/wards).
     */
    @GetMapping("/checkout")
    public String checkoutPage(@RequestHeader(value = "Referer", required = false) String referer,
                               Model model) {
        
        // Kiểm tra luồng: Phải đi từ /cart (trừ khi đang ở chính trang /checkout - refresh)
        if (referer == null || (!referer.contains("/cart") && !referer.contains("/checkout"))) {
            log.warn("[Access Control] Ngăn chặn truy cập trực tiếp trang checkout. Referer: {}", referer);
            return "redirect:/cart";
        }

        CheckoutRequestDTO checkoutRequest = new CheckoutRequestDTO();

        try {
            // Lấy thông tin user đang đăng nhập (nếu có) để pre-fill form
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated() && !auth.getPrincipal().equals("anonymousUser")) {
                String email = auth.getName(); // Spring Security mặc định dùng email/username làm Name
                Optional<fpt.legendcoffee.entity.User> userOpt = userRepository.findByEmail(email);

                userOpt.ifPresent(u -> {
                    checkoutRequest.setRecipientName(u.getUsername()); // fullname
                    checkoutRequest.setRecipientPhone(u.getPhone());
                    checkoutRequest.setRecipientAddress(u.getAddress());
                    log.info("[Checkout] Pre-filled user data for: {}", email);
                });
            }

            model.addAttribute("provinces", shippingService.getProvinces());
        } catch (Exception e) {
            log.warn("[Checkout] Không thể tải dữ liệu khởi tạo: {}", e.getMessage());
            model.addAttribute("provinces", java.util.Collections.emptyList());
            model.addAttribute("warningMessage", "Dịch vụ vận chuyển đang gặp sự cố. Bạn vẫn có thể nhập địa chỉ thủ công.");
        }

        model.addAttribute("checkoutRequest", checkoutRequest);
        return "cart/checkout";
    }

    /**
     * POST /checkout/place-order
     * Xử lý đặt hàng:
     *  1. Lưu Order vào DB, lấy orderId
     *  2. Tạo đơn GHN
     *  3. Redirect sang trang xác nhận
     */
    @PostMapping("/checkout/place-order")
    public String placeOrder(@Valid @ModelAttribute("checkoutRequest") CheckoutRequestDTO checkout,
                             BindingResult bindingResult,
                             Model model,
                             RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("provinces", shippingService.getProvinces());
            return "cart/checkout";
        }

        try {

            // Debug: Log cart data
            log.info("[Checkout] Cart JSON: {}", checkout.getCartJson());

            // TODO: Parse cartJson and use for order creation
            // Bước 1 — Lưu Order vào DB, gán orderId vào checkout
            Order order = orderService.createOrder(checkout);
            checkout.setOrderId(order.getId());

            // Bước 2 — Tạo đơn GHN (yêu cầu checkout.orderId đã được gán)
            ShippingInfo shippingInfo = shippingService.createGHNOrder(checkout);

            log.info("[Checkout] Đặt hàng thành công. GHN code: {}", shippingInfo.getGhnOrderCode());
            redirectAttributes.addFlashAttribute("successMessage",
                    "Đặt hàng thành công! Mã vận chuyển: " + shippingInfo.getGhnOrderCode());

            // Mặc định route "/orders/{id}" không tồn tại và bị chặn bởi Security -> Redirect sang trang tracking
            return "redirect:/orders/" + checkout.getOrderId() + "/track";

        } catch (Exception e) {
            log.error("[Checkout] Đặt hàng thất bại: {}", e.getMessage(), e);
            model.addAttribute("errorMessage", "Đặt hàng thất bại: " + e.getMessage());
            model.addAttribute("provinces", shippingService.getProvinces());
            return "cart/checkout";
        }
    }

    // =========================================================================
    // Tracking đơn hàng
    // =========================================================================

    /**
     * GET /orders/{orderId}/track
     * Trang theo dõi trạng thái vận chuyển cho khách hàng.
     */
    @GetMapping("/orders/{orderId}/track")
    public String trackOrder(@PathVariable Long orderId, Model model) {
        try {
            model.addAttribute("orderStatus", shippingService.getOrderStatus(orderId));
            model.addAttribute("orderId", orderId);
        } catch (Exception e) {
            log.warn("[Tracking] Không tìm thấy thông tin vận chuyển cho orderId={}", orderId);
            model.addAttribute("errorMessage", "Không tìm thấy thông tin đơn hàng");
        }
        return "order/order-tracking"; // templates/order/order-tracking.html
    }

    // =========================================================================
    // Huỷ đơn hàng
    // =========================================================================

    /**
     * POST /orders/{orderId}/cancel
     * Huỷ đơn hàng (chỉ được khi chưa lấy hàng).
     */
    @PostMapping("/orders/{orderId}/cancel")
    public String cancelOrder(@PathVariable Long orderId,
                              RedirectAttributes redirectAttributes) {
        try {
            boolean success = shippingService.cancelShipping(orderId);
            if (success) {
                redirectAttributes.addFlashAttribute("successMessage", "Đã huỷ đơn hàng thành công");
            } else {
                redirectAttributes.addFlashAttribute("errorMessage",
                        "Không thể huỷ đơn. Đơn hàng đang trong quá trình vận chuyển.");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
        }
        return "redirect:/orders/" + orderId + "/track";
    }
}

