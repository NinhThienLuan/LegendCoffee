package fpt.legendcoffee.service.serviceImpl;

import fpt.legendcoffee.dto.app.CheckoutItemRequestDTO;
import fpt.legendcoffee.dto.app.CheckoutRequestDTO;
import fpt.legendcoffee.entity.Combo;
import fpt.legendcoffee.entity.Order;
import fpt.legendcoffee.entity.OrderItem;
import fpt.legendcoffee.entity.ProductVariant;
import fpt.legendcoffee.entity.User;
import fpt.legendcoffee.entity.ShippingInfo;
import fpt.legendcoffee.entity.Voucher;
import fpt.legendcoffee.entity.VariantPromotion;
import fpt.legendcoffee.entity.Promotion;
import fpt.legendcoffee.dto.app.OrderListDTO;
import fpt.legendcoffee.dto.app.OrderStatusDTO;
import fpt.legendcoffee.entity.enumeration.OrderStatus;
import fpt.legendcoffee.entity.enumeration.PaymentStatus;
import fpt.legendcoffee.entity.Payment;
import fpt.legendcoffee.repository.ComboRepository;
import fpt.legendcoffee.repository.OrderItemRepository;
import fpt.legendcoffee.repository.OrderRepository;
import fpt.legendcoffee.repository.ProductVariantRepository;
import fpt.legendcoffee.repository.UserRepository;
import fpt.legendcoffee.repository.ShippingInfoRepository;
import fpt.legendcoffee.repository.VoucherRepository;
import fpt.legendcoffee.repository.VariantPromotionRepository;
import fpt.legendcoffee.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final ProductVariantRepository productVariantRepository;
    private final ComboRepository comboRepository;
    private final UserRepository userRepository;
    private final ShippingInfoRepository shippingInfoRepository;
    private final VoucherRepository voucherRepository;
    private final VariantPromotionRepository variantPromotionRepository;

    @Override
    @Transactional
    public Order createOrder(CheckoutRequestDTO checkout) {
        log.info("[OrderService] Creating new order for recipient: {}", checkout.getRecipientName());

        List<OrderItem> draftItems = buildOrderItems(checkout.getItems());
        BigDecimal subTotal = draftItems.stream()
                .map(OrderItem::getSubTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal shippingFee = BigDecimal.valueOf(checkout.getShippingFee() != null ? checkout.getShippingFee() : 0);
        Optional<User> currentUser = getCurrentUser();

        // Xử lý voucher nếu có
        Voucher appliedVoucher = null;
        BigDecimal voucherDiscount = BigDecimal.ZERO;
        if (checkout.getVoucherCode() != null && !checkout.getVoucherCode().isBlank()) {
            appliedVoucher = validateAndApplyVoucher(checkout.getVoucherCode(), subTotal);
            if (appliedVoucher != null) {
                voucherDiscount = calculateVoucherDiscount(appliedVoucher, subTotal);
                log.info("[OrderService] Applied voucher: {} with discount: {}", checkout.getVoucherCode(), voucherDiscount);
            }
        }

        // Tính tổng discount (voucher + promotion trong items)
        BigDecimal promotionDiscount = draftItems.stream()
                .map(OrderItem::getPromotionDiscount)
                .filter(d -> d != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalDiscount = voucherDiscount.add(promotionDiscount);
        BigDecimal finalAmount = subTotal.subtract(totalDiscount).add(shippingFee);

        Order order = Order.builder()
            .user(currentUser.orElse(null))
                .orderDate(LocalDateTime.now())
                .subTotal(subTotal)
                .discount(totalDiscount)
                .totalAmount(finalAmount)
                .voucher(appliedVoucher)
                .status(OrderStatus.PENDING)
                .build();

        Order savedOrder = orderRepository.save(order);

        if (!draftItems.isEmpty()) {
            draftItems.forEach(item -> {
                item.setOrder(savedOrder);
                item.setStatus(OrderStatus.PENDING.name());
            });
            orderItemRepository.saveAll(draftItems);
        }

        // Increment voucher usage count
        if (appliedVoucher != null) {
            appliedVoucher.setUsedCount((appliedVoucher.getUsedCount() != null ? appliedVoucher.getUsedCount() : 0) + 1);
            voucherRepository.save(appliedVoucher);
            log.info("[OrderService] Incremented voucher usage count for: {}", checkout.getVoucherCode());
        }

        log.info("[OrderService] Created order successfully with ID: {}", savedOrder.getId());

        return savedOrder;
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderListDTO> getAllOrdersForList() {
        List<Order> orders = orderRepository.findAllWithDetails();
        return mapOrdersToDTO(orders);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderListDTO> getOrdersByStatus(OrderStatus status) {
        List<Order> orders = orderRepository.findByStatusWithDetails(status);
        return mapOrdersToDTO(orders);
    }

    private List<OrderListDTO> mapOrdersToDTO(List<Order> orders) {
        return orders.stream().map(order -> {
            List<OrderItem> items = order.getOrderItems();
            String itemName = "Sản phẩm";
            String itemDetail = "";
            String firstImage = null;
            List<String> allImages = new ArrayList<>();
            int additionalCount = 0;

            if (items != null && !items.isEmpty()) {
                // Lấy ảnh từ sản phẩm hoặc combo
                for (OrderItem item : items) {
                    String img = null;
                    if (item.getVariant() != null) {
                        img = item.getVariant().getImageUrl();
                        if (img == null && item.getVariant().getProduct() != null) {
                            img = item.getVariant().getProduct().getImageUrl();
                        }
                    } else if (item.getCombo() != null) {
                        // Lấy ảnh từ sản phẩm đầu tiên trong combo
                        if (item.getCombo().getComboItems() != null && !item.getCombo().getComboItems().isEmpty()) {
                            fpt.legendcoffee.entity.ComboItem firstItem = item.getCombo().getComboItems().iterator().next();
                            if (firstItem.getVariant() != null) {
                                img = firstItem.getVariant().getImageUrl();
                                if (img == null && firstItem.getVariant().getProduct() != null) {
                                    img = firstItem.getVariant().getProduct().getImageUrl();
                                }
                            }
                        }
                    }

                    if (img != null && !allImages.contains(img)) {
                        allImages.add(img);
                    }
                }

                // Lấy thông tin từ item đầu tiên
                OrderItem first = items.stream()
                        .filter(i -> i.getVariant() != null || i.getCombo() != null)
                        .findFirst()
                        .orElse(items.get(0));

                if (first.getVariant() != null) {
                    itemName = first.getVariant().getProduct() != null
                            ? first.getVariant().getProduct().getName()
                            : first.getVariant().getVariantName();
                    itemDetail = first.getVariant().getVariantName();
                } else if (first.getCombo() != null) {
                    itemName = first.getCombo().getName();
                    itemDetail = "Combo Legend";
                }
                firstImage = allImages.isEmpty() ? null : allImages.get(0);

                additionalCount = items.size() - 1;
            }

            // Lấy trạng thái shipping
            ShippingInfo shipping = order.getShippingInfo();
            String shippingStatus = shipping != null ? shipping.getStatus() : "pending";

            // Lấy URL thanh toán VNPay nếu đang PENDING
            String paymentUrl = null;
            if (order.getPayments() != null) {
                paymentUrl = order.getPayments().stream()
                        .filter(p -> PaymentStatus.PENDING.equals(p.getStatus()))
                        .filter(p -> p.getCreatedAt() == null || p.getCreatedAt().plusMinutes(15).isAfter(LocalDateTime.now()))
                        .map(Payment::getPaymentUrl)
                        .findFirst()
                        .orElse(null);
            }

            return OrderListDTO.builder()
                    .id(order.getId())
                    .orderDate(order.getOrderDate())
                    .totalAmount(order.getTotalAmount())
                    .firstItemName(itemName)
                    .firstItemDetail(itemDetail)
                    .firstItemImage(firstImage)
                    .itemImages(allImages)
                    .additionalItemsCount(additionalCount)
                    .shippingStatus(shippingStatus)
                    .shippingStatusLabel(OrderStatusDTO.mapStatusLabel(shippingStatus))
                    .paymentUrl(paymentUrl)
                    .build();
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void startDelivering(Long orderId) {
        ShippingInfo info = shippingInfoRepository.findByOrderId(orderId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy thông tin vận chuyển cho đơn #" + orderId));

        if (!"ready_to_pick".equalsIgnoreCase(info.getStatus())) {
            throw new RuntimeException("Chỉ có thể giao khi trạng thái đang là Chờ lấy hàng");
        }

        info.setStatus("delivering");
        shippingInfoRepository.save(info);
    }

    @Override
    @Transactional(readOnly = true)
    public Order getOrderWithDetails(Long id) {
        return orderRepository.findByIdWithDetails(id).orElse(null);
    }


    private List<OrderItem> buildOrderItems(List<CheckoutItemRequestDTO> itemRequests) {
        if (itemRequests == null || itemRequests.isEmpty()) {
            return List.of();
        }

        List<OrderItem> items = new ArrayList<>();
        for (CheckoutItemRequestDTO request : itemRequests) {
            int quantity = request.getQuantity() != null ? request.getQuantity() : 0;
            if (quantity <= 0) {
                throw new IllegalArgumentException("Số lượng sản phẩm phải lớn hơn 0");
            }

            boolean hasVariant = request.getVariantId() != null;
            boolean hasCombo = request.getComboId() != null;
            if (hasVariant == hasCombo) {
                throw new IllegalArgumentException("Mỗi item checkout phải có đúng một trong hai: variantId hoặc comboId");
            }

            BigDecimal unitPrice;
            ProductVariant variant = null;
            Combo combo = null;
            BigDecimal promotionDiscount = BigDecimal.ZERO;

            if (hasVariant) {
                variant = productVariantRepository.findById(request.getVariantId())
                        .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy biến thể với ID: " + request.getVariantId()));
                if (!Boolean.TRUE.equals(variant.getIsActive())) {
                    throw new IllegalArgumentException("Biến thể với ID " + request.getVariantId() + " đang không hoạt động");
                }
                unitPrice = variant.getPrice();

                // Tìm promotion cho variant này
                promotionDiscount = calculateVariantPromotionDiscount(variant, unitPrice, quantity);
            } else {
                combo = comboRepository.findById(request.getComboId())
                        .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy combo với ID: " + request.getComboId()));
                if (!Boolean.TRUE.equals(combo.getIsActive())) {
                    throw new IllegalArgumentException("Combo với ID " + request.getComboId() + " đang không hoạt động");
                }
                LocalDateTime now = LocalDateTime.now();
                if (combo.getStartDate() != null && now.isBefore(combo.getStartDate())) {
                    throw new IllegalArgumentException("Combo với ID " + request.getComboId() + " chưa đến thời gian áp dụng");
                }
                if (combo.getEndDate() != null && now.isAfter(combo.getEndDate())) {
                    throw new IllegalArgumentException("Combo với ID " + request.getComboId() + " đã hết thời gian áp dụng");
                }
                unitPrice = combo.getPrice();
            }

            if (unitPrice == null || unitPrice.compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("Item checkout có giá không hợp lệ");
            }

            BigDecimal subTotal = unitPrice.multiply(BigDecimal.valueOf(quantity));
            items.add(OrderItem.builder()
                    .variant(variant)
                    .combo(combo)
                    .quantity(quantity)
                    .unitPrice(unitPrice)
                    .subTotal(subTotal)
                    .discount(BigDecimal.ZERO)
                    .promotionDiscount(promotionDiscount)
                    .totalAmount(subTotal.subtract(promotionDiscount))
                    .build());
        }

        return items;
    }

    private Optional<User> getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return Optional.empty();
        }
        return userRepository.findByEmail(auth.getName());
    }

    /**
     * Validate và apply voucher code
     */
    private Voucher validateAndApplyVoucher(String voucherCode, BigDecimal orderAmount) {
        Voucher voucher = voucherRepository.findByCode(voucherCode)
                .orElse(null);

        if (voucher == null) {
            log.warn("[OrderService] Voucher not found: {}", voucherCode);
            return null;
        }

        LocalDateTime now = LocalDateTime.now();

        // Kiểm tra voucher hợp lệ
        if (!Boolean.TRUE.equals(voucher.getActive())) {
            log.warn("[OrderService] Voucher is not active: {}", voucherCode);
            return null;
        }

        if (voucher.getStartDate() != null && now.isBefore(voucher.getStartDate())) {
            log.warn("[OrderService] Voucher not yet valid: {}", voucherCode);
            return null;
        }

        if (voucher.getEndDate() != null && now.isAfter(voucher.getEndDate())) {
            log.warn("[OrderService] Voucher has expired: {}", voucherCode);
            return null;
        }

        if (voucher.getUsageLimit() != null && voucher.getUsedCount() != null &&
            voucher.getUsedCount() >= voucher.getUsageLimit()) {
            log.warn("[OrderService] Voucher usage limit reached: {}", voucherCode);
            return null;
        }

        if (voucher.getConditionMin() != null &&
            orderAmount.compareTo(voucher.getConditionMin()) < 0) {
            log.warn("[OrderService] Order amount {} is less than voucher condition: {}", orderAmount, voucher.getConditionMin());
            return null;
        }

        log.info("[OrderService] Voucher validated successfully: {}", voucherCode);
        return voucher;
    }

    /**
     * Tính discount từ voucher
     */
    private BigDecimal calculateVoucherDiscount(Voucher voucher, BigDecimal baseAmount) {
        if (voucher == null || voucher.getValue() == null) {
            return BigDecimal.ZERO;
        }

        String voucherType = voucher.getType().name();
        BigDecimal discount = BigDecimal.ZERO;

        if ("PERCENT".equals(voucherType)) {
            // Giảm theo phần trăm
            discount = baseAmount.multiply(voucher.getValue()).divide(BigDecimal.valueOf(100), 2, java.math.RoundingMode.HALF_UP);
        } else if ("FIXED".equals(voucherType)) {
            // Giảm số tiền cố định
            discount = voucher.getValue();
        }

        return discount;
    }

    /**
     * Tính promotion discount cho một variant
     */
    private BigDecimal calculateVariantPromotionDiscount(ProductVariant variant, BigDecimal unitPrice, int quantity) {
        if (variant == null || variant.getId() == null) {
            return BigDecimal.ZERO;
        }

        List<VariantPromotion> variantPromotions = variantPromotionRepository.findByVariantId(variant.getId());

        for (VariantPromotion vp : variantPromotions) {
            Promotion promotion = vp.getPromotion();
            if (promotion == null) {
                continue;
            }

            LocalDateTime now = LocalDateTime.now();

            // Kiểm tra promotion có hợp lệ không
            if (promotion.getStartDate() != null && now.isBefore(promotion.getStartDate())) {
                continue;
            }
            if (promotion.getEndDate() != null && now.isAfter(promotion.getEndDate())) {
                continue;
            }

            // Tính discount
            BigDecimal discount = BigDecimal.ZERO;
            String promotionType = promotion.getType();

            if ("PERCENT".equals(promotionType)) {
                // Giảm theo phần trăm
                BigDecimal itemTotal = unitPrice.multiply(BigDecimal.valueOf(quantity));
                discount = itemTotal.multiply(promotion.getValue()).divide(BigDecimal.valueOf(100), 2, java.math.RoundingMode.HALF_UP);
            } else if ("FIXED".equals(promotionType)) {
                // Giảm số tiền cố định trên mỗi item
                discount = promotion.getValue().multiply(BigDecimal.valueOf(quantity));
            }

            if (discount.compareTo(BigDecimal.ZERO) > 0) {
                log.info("[OrderService] Applied promotion to variant {}: discount {}", variant.getId(), discount);
                return discount;
            }
        }

        return BigDecimal.ZERO;
    }
}
