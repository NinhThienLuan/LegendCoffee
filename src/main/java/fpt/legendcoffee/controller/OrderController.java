package fpt.legendcoffee.controller;

import fpt.legendcoffee.common.util.WebUtils;
import fpt.legendcoffee.dto.app.CheckoutRequestDTO;
import fpt.legendcoffee.dto.app.OrderListDTO;
import fpt.legendcoffee.entity.Order;
import fpt.legendcoffee.entity.OrderItem;
import fpt.legendcoffee.entity.ShippingInfo;
import fpt.legendcoffee.entity.User;
import fpt.legendcoffee.entity.enumeration.OrderStatus;
import fpt.legendcoffee.repository.OrderItemRepository;
import fpt.legendcoffee.repository.OrderRepository;
import fpt.legendcoffee.repository.PaymentRepository;
import fpt.legendcoffee.repository.ShippingInfoRepository;
import fpt.legendcoffee.repository.UserRepository;
import fpt.legendcoffee.service.OrderService;
import fpt.legendcoffee.service.ShippingService;
import fpt.legendcoffee.service.VNPayApplicationService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import java.time.LocalDateTime;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Slf4j
@Controller
@RequiredArgsConstructor
public class OrderController {

    private final ShippingService shippingService;
    private final OrderService orderService;
    private final VNPayApplicationService vnPayApplicationService;
    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final ShippingInfoRepository shippingInfoRepository;
    private final PaymentRepository paymentRepository;

    // =========================================================================
    // Trang danh sách đơn hàng & chi tiết
    // =========================================================================

    @GetMapping("/orders")
    public String orderPage(@RequestParam(required = false) String status, Model model) {
        Optional<User> currentUser = getCurrentUser();
        if (currentUser.isEmpty()) {
            return "redirect:/login";
        }

        boolean isAdmin = SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        // Parse status filter
        OrderStatus orderStatus = null;
        if (status != null && !status.isBlank()) {
            try {
                orderStatus = OrderStatus.valueOf(status.toUpperCase());
            } catch (IllegalArgumentException ignored) {
            }
        }

        List<OrderListDTO> allOrders;
        if (orderStatus != null) {
            allOrders = orderService.getOrdersByStatus(orderStatus);
        } else {
            allOrders = orderService.getAllOrdersForList();
        }

        // Non-admin users can only see their own orders
        if (!isAdmin) {
            final Long userId = currentUser.get().getId();
            allOrders = allOrders.stream().filter(dto -> orderRepository.findById(dto.getId())
                    .map(Order::getUser)
                    .map(User::getId).orElse(-1L).equals(userId)).toList();
        }

        model.addAttribute("orders", allOrders);
        model.addAttribute("selectedStatus", status != null ? status.toUpperCase() : "");

        if (isAdmin) {
            return "admin/orders";
        }
        return "order/order";
    }

    @PostMapping("/admin/orders/{orderId}/start-delivering")
    public String startDelivering(@PathVariable Long orderId, RedirectAttributes redirectAttributes) {
        try {
            orderService.startDelivering(orderId);
            redirectAttributes.addFlashAttribute("successMessage", "Đã chuyển sang trạng thái Đang giao hàng");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/orders";
    }

    @PostMapping("/admin/orders/{orderId}/complete-delivery")
    public String completeDelivery(@PathVariable Long orderId, RedirectAttributes redirectAttributes) {
        try {
            orderService.completeDelivery(orderId);
            redirectAttributes.addFlashAttribute("successMessage", "Đã xác nhận khách hàng nhận hàng thành công");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/orders";
    }

    @GetMapping("/orders/{orderId}")
    public String orderDetailPage(@PathVariable("orderId") Long orderId, Model model,
            RedirectAttributes redirectAttributes) {
        Optional<User> currentUser = getCurrentUser();
        if (currentUser.isEmpty()) {
            return "redirect:/login";
        }

        Optional<Order> orderOpt = orderRepository.findByIdWithUser(orderId);
        if (orderOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy đơn hàng.");
            return "redirect:/orders";
        }

        Order order = orderOpt.get();
        // Ownership check
        if (order.getUser() == null || !order.getUser().getId().equals(currentUser.get().getId())) {
            redirectAttributes.addFlashAttribute("errorMessage", "Bạn không có quyền xem đơn hàng này.");
            return "redirect:/orders";
        }

        List<OrderItem> orderItems = orderItemRepository.findByOrderIdWithDetails(orderId);
        ShippingInfo shippingInfo = shippingInfoRepository.findByOrderId(orderId).orElse(null);

        BigDecimal shippingFee = shippingInfo != null && shippingInfo.getShippingFee() != null
                ? BigDecimal.valueOf(shippingInfo.getShippingFee())
                : BigDecimal.ZERO;
        BigDecimal subTotal = order.getSubTotal() != null ? order.getSubTotal() : BigDecimal.ZERO;
        BigDecimal totalAmount = order.getTotalAmount() != null ? order.getTotalAmount() : subTotal.add(shippingFee);
        BigDecimal vat = totalAmount.subtract(subTotal).subtract(shippingFee).max(BigDecimal.ZERO);

        try {
            model.addAttribute("order", order);
            model.addAttribute("orderItems", orderItems);
            model.addAttribute("shippingInfo", shippingInfo);
            model.addAttribute("shippingFee", shippingFee);
            model.addAttribute("vat", vat);
            model.addAttribute("totalAmount", totalAmount);
            model.addAttribute("orderId", orderId);

            // Tìm URL thanh toán VNPay nếu đơn hàng đang chờ thanh toán
            paymentRepository.findByOrderIdAndStatus(orderId, fpt.legendcoffee.entity.enumeration.PaymentStatus.PENDING)
                    .filter(p -> p.getCreatedAt() == null
                            || p.getCreatedAt().plusMinutes(15).isAfter(LocalDateTime.now()))
                    .ifPresent(p -> model.addAttribute("paymentUrl", p.getPaymentUrl()));

            // Tích hợp dữ liệu tracking trực tiếp vào trang detail
            try {
                model.addAttribute("orderStatus", shippingService.getOrderStatus(orderId));
            } catch (Exception e) {
                log.warn("[Tracking] Không tìm thấy thông tin vận chuyển cho orderId={}", orderId);
            }

            return "order/order-detail";
        } catch (Exception e) {
            log.error("Error rendering order-detail for ID {}: {}", orderId, e.getMessage());
            return "redirect:/orders";
        }
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
     * Quận/huyện và phường/xã được tải động qua AJAX (/api/shipping/districts &
     * /api/shipping/wards).
     */
    @GetMapping("/checkout")
    public String checkoutPage(@RequestHeader(value = "Referer", required = false) String referer,
            HttpServletResponse response,
            Model model) {

        // Ngăn chặn cache để nút Back không load lại trang checkout cũ
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        response.setHeader("Pragma", "no-cache");
        response.setHeader("Expires", "0");

        // Kiểm tra luồng: Phải đi từ /cart (trừ khi đang ở chính trang /checkout -
        // refresh)
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
            model.addAttribute("warningMessage",
                    "Dịch vụ vận chuyển đang gặp sự cố. Bạn vẫn có thể nhập địa chỉ thủ công.");
        }

        model.addAttribute("checkoutRequest", checkoutRequest);
        model.addAttribute("maxItemQty", orderService.getMaxQuantityPerItem());
        model.addAttribute("maxTotalQty", orderService.getMaxTotalQuantity());
        return "cart/checkout";
    }

    /**
     * POST /checkout/place-order
     * Xử lý đặt hàng:
     * 1. Lưu Order vào DB, lấy orderId
     * 2. Tạo đơn GHN
     * 3. Redirect sang danh sách đơn hàng
     */
    @PostMapping("/checkout/place-order")
    public String placeOrder(@Valid @ModelAttribute("checkoutRequest") CheckoutRequestDTO checkout,
            BindingResult bindingResult,
            HttpServletRequest request,
            Model model,
            RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("provinces", shippingService.getProvinces());
            model.addAttribute("maxItemQty", orderService.getMaxQuantityPerItem());
            model.addAttribute("maxTotalQty", orderService.getMaxTotalQuantity());
            model.addAttribute("checkoutError", true);
            return "cart/checkout";
        }

        try {
            log.info("[Checkout] Số lượng dòng sản phẩm gửi lên: {}",
                    checkout.getItems() != null ? checkout.getItems().size() : 0);

            // Bước 1 — Lưu Order vào DB, gán orderId vào checkout
            Order order = orderService.createOrder(checkout);

            // Bước 2 — Lưu thông tin vận chuyển (chưa đẩy sang GHN)
            shippingService.saveShippingInfo(checkout, order);

            // Bước 3 — Chuyển hướng sang thanh toán online qua VNPay
            String ipAddress = WebUtils.getClientIp(request);
            String paymentUrl = vnPayApplicationService.createPayment(order.getId(), ipAddress);

            log.info("[Checkout] Đặt hàng thành công. Chuyển hướng sang VNPay cho OrderId={}", order.getId());
            return "redirect:" + paymentUrl;

        } catch (Exception e) {
            log.error("[Checkout] Đặt hàng thất bại: {}", e.getMessage(), e);
            model.addAttribute("errorMessage", "Đặt hàng thất bại: " + e.getMessage());
            model.addAttribute("provinces", shippingService.getProvinces());
            model.addAttribute("maxItemQty", orderService.getMaxQuantityPerItem());
            model.addAttribute("maxTotalQty", orderService.getMaxTotalQuantity());
            model.addAttribute("checkoutError", true);
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
    public String trackOrder(@PathVariable Long orderId, Model model, RedirectAttributes redirectAttributes) {
        Optional<User> currentUser = getCurrentUser();
        if (currentUser.isEmpty()) {
            return "redirect:/login";
        }

        Optional<Order> orderOpt = orderRepository.findById(orderId);
        if (orderOpt.isEmpty() || orderOpt.get().getUser() == null
                || !orderOpt.get().getUser().getId().equals(currentUser.get().getId())) {
            redirectAttributes.addFlashAttribute("errorMessage", "Bạn không có quyền theo dõi đơn hàng này.");
            return "redirect:/orders";
        }

        try {
            model.addAttribute("orderStatus", shippingService.getOrderStatus(orderId));
            model.addAttribute("orderId", orderId);
        } catch (Exception e) {
            log.warn("[Tracking] Không tìm thấy thông tin vận chuyển cho orderId={}", orderId);
            model.addAttribute("errorMessage", "Không tìm thấy thông tin đơn hàng");
        }
        return "order/order-tracking";
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
            HttpServletRequest request,
            RedirectAttributes redirectAttributes) {
        Optional<User> currentUser = getCurrentUser();
        if (currentUser.isEmpty()) {
            return "redirect:/login";
        }

        Optional<Order> orderOpt = orderRepository.findById(orderId);
        if (orderOpt.isEmpty() || orderOpt.get().getUser() == null
                || !orderOpt.get().getUser().getId().equals(currentUser.get().getId())) {
            redirectAttributes.addFlashAttribute("errorMessage", "Bạn không có quyền huỷ đơn hàng này.");
            return "redirect:/orders";
        }

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

        String referer = request.getHeader("Referer");
        if (referer != null && referer.contains("/orders") && !referer.contains("/orders/")) {
            return "redirect:/orders";
        }
        return "redirect:/orders/" + orderId;
    }

    private Optional<User> getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return Optional.empty();
        }
        return userRepository.findByEmail(auth.getName());
    }
}
