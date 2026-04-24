package fpt.legendcoffee.service.serviceImpl;

import fpt.legendcoffee.dto.app.CheckoutItemRequestDTO;
import fpt.legendcoffee.dto.app.CheckoutRequestDTO;
import fpt.legendcoffee.entity.Combo;
import fpt.legendcoffee.entity.Order;
import fpt.legendcoffee.entity.OrderItem;
import fpt.legendcoffee.entity.ProductVariant;
import fpt.legendcoffee.entity.User;
import fpt.legendcoffee.entity.ShippingInfo;
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

    private static final int MAX_QUANTITY_PER_ITEM = 10;
    private static final int MAX_TOTAL_QUANTITY = 30;

    @Override
    @Transactional
    public Order createOrder(CheckoutRequestDTO checkout) {
        log.info("[OrderService] Creating new order for recipient: {}", checkout.getRecipientName());

        List<OrderItem> draftItems = buildOrderItems(checkout.getItems());

        // 1. Validate total quantity
        int totalQuantity = draftItems.stream().mapToInt(OrderItem::getQuantity).sum();
        if (totalQuantity > MAX_TOTAL_QUANTITY) {
            throw new IllegalArgumentException("Đặt quá giới hạn online. Vui lòng liên hệ tổng đài để được hỗ trợ");
        }

        BigDecimal subTotal = draftItems.stream()
                .map(OrderItem::getSubTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal shippingFee = BigDecimal.valueOf(checkout.getShippingFee() != null ? checkout.getShippingFee() : 0);
        Optional<User> currentUser = getCurrentUser();

        Order order = Order.builder()
                .user(currentUser.orElse(null))
                .orderDate(LocalDateTime.now())
                .subTotal(subTotal)
                .discount(BigDecimal.ZERO)
                .totalAmount(subTotal.add(shippingFee))
                .status(OrderStatus.PENDING)
                .build();

        // 2. Deduct Stock
        for (OrderItem item : draftItems) {
            if (item.getVariant() != null) {
                deductVariantStock(item.getVariant(), item.getQuantity());
            } else if (item.getCombo() != null) {
                deductComboStock(item.getCombo(), item.getQuantity());
            }
        }

        Order savedOrder = orderRepository.save(order);

        if (!draftItems.isEmpty()) {
            draftItems.forEach(item -> {
                item.setOrder(savedOrder);
                item.setStatus(OrderStatus.PENDING.name());
            });
            orderItemRepository.saveAll(draftItems);
        }

        log.info("[OrderService] Created order successfully with ID: {}", savedOrder.getId());

        return savedOrder;
    }

    private void deductVariantStock(ProductVariant variant, int quantity) {
        int currentStock = variant.getStockQuantity() != null ? variant.getStockQuantity() : 0;
        if (currentStock < quantity) {
            throw new IllegalArgumentException(
                    "Sản phẩm '" + variant.getVariantName() + "' đã hết hàng hoặc không đủ số lượng");
        }
        variant.setStockQuantity(currentStock - quantity);
        productVariantRepository.save(variant);
    }

    private void deductComboStock(Combo combo, int comboQuantity) {
        for (fpt.legendcoffee.entity.ComboItem comboItem : combo.getComboItems()) {
            int requiredQuantity = comboItem.getQuantity() * comboQuantity;
            deductVariantStock(comboItem.getVariant(), requiredQuantity);
        }
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
                            fpt.legendcoffee.entity.ComboItem firstItem = item.getCombo().getComboItems().iterator()
                                    .next();
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
                        .filter(p -> p.getCreatedAt() == null
                                || p.getCreatedAt().plusMinutes(15).isAfter(LocalDateTime.now()))
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
            throw new RuntimeException("Chỉ có thể giao khi trạng thái đang là Chờ lấy hàng (ready_to_pick)");
        }

        info.setStatus("delivering");
        shippingInfoRepository.save(info);

        // Update Order status to SHIPPING
        Order order = info.getOrder();
        if (order != null) {
            order.setStatus(OrderStatus.SHIPPING);
            orderRepository.save(order);
        }
    }

    @Override
    @Transactional
    public void completeDelivery(Long orderId) {
        ShippingInfo info = shippingInfoRepository.findByOrderId(orderId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy thông tin vận chuyển cho đơn #" + orderId));

        if (!"delivering".equalsIgnoreCase(info.getStatus())) {
            throw new RuntimeException(
                    "Chỉ có thể xác nhận nhận hàng khi trạng thái đang là Đang giao hàng (delivering)");
        }

        info.setStatus("delivered");
        shippingInfoRepository.save(info);

        // Update Order status to COMPLETED
        Order order = info.getOrder();
        if (order != null) {
            order.setStatus(OrderStatus.COMPLETED);
            orderRepository.save(order);
        }
    }

    @Override
    @Transactional
    public void cancelOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng #" + orderId));

        if (OrderStatus.CANCELLED.equals(order.getStatus())) {
            log.info("[OrderService] Order #{} is already cancelled", orderId);
            return;
        }

        log.info("[OrderService] Cancelling order #{} and restoring stock", orderId);
        order.setStatus(OrderStatus.CANCELLED);
        
        // Cập nhật trạng thái vận chuyển đồng bộ
        if (order.getShippingInfo() != null) {
            order.getShippingInfo().setStatus("cancel");
        }
        
        orderRepository.save(order);

        // Restore stock
        if (order.getOrderItems() != null) {
            for (OrderItem item : order.getOrderItems()) {
                if (item.getVariant() != null) {
                    restoreVariantStock(item.getVariant(), item.getQuantity());
                } else if (item.getCombo() != null) {
                    restoreComboStock(item.getCombo(), item.getQuantity());
                }
            }
        }
    }

    private void restoreVariantStock(ProductVariant variant, int quantity) {
        int currentStock = variant.getStockQuantity() != null ? variant.getStockQuantity() : 0;
        variant.setStockQuantity(currentStock + quantity);
        productVariantRepository.save(variant);
    }

    private void restoreComboStock(Combo combo, int comboQuantity) {
        for (fpt.legendcoffee.entity.ComboItem comboItem : combo.getComboItems()) {
            int quantityToRestore = comboItem.getQuantity() * comboQuantity;
            restoreVariantStock(comboItem.getVariant(), quantityToRestore);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Order getOrderWithDetails(Long id) {
        return orderRepository.findByIdWithDetails(id).orElse(null);
    }

    @Override
    public int getMaxQuantityPerItem() {
        return MAX_QUANTITY_PER_ITEM;
    }

    @Override
    public int getMaxTotalQuantity() {
        return MAX_TOTAL_QUANTITY;
    }

    @Override
    public Optional<Order> findById(Long orderId) {
        return orderRepository.findById(orderId);
    }

    @Override
    public Optional<Order> findByIdWithUser(Long orderId) {
        return orderRepository.findByIdWithUser(orderId);
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
            if (quantity > MAX_QUANTITY_PER_ITEM) {
                throw new IllegalArgumentException("Đặt quá giới hạn online. Vui lòng liên hệ tổng đài để được hỗ trợ");
            }

            boolean hasVariant = request.getVariantId() != null;
            boolean hasCombo = request.getComboId() != null;
            if (hasVariant == hasCombo) {
                throw new IllegalArgumentException(
                        "Mỗi item checkout phải có đúng một trong hai: variantId hoặc comboId");
            }

            BigDecimal unitPrice;
            ProductVariant variant = null;
            Combo combo = null;

            if (hasVariant) {
                variant = productVariantRepository.findById(request.getVariantId())
                        .orElseThrow(() -> new IllegalArgumentException(
                                "Không tìm thấy biến thể với ID: " + request.getVariantId()));
                if (!Boolean.TRUE.equals(variant.getIsActive())) {
                    throw new IllegalArgumentException(
                            "Sản phẩm '" + variant.getVariantName() + "' đang ngừng kinh doanh");
                }
                if (variant.getProduct() != null && !Boolean.TRUE.equals(variant.getProduct().getIsActive())) {
                    throw new IllegalArgumentException(
                            "Sản phẩm '" + variant.getProduct().getName() + "' đang ngừng kinh doanh");
                }
                unitPrice = variant.getPrice();
            } else {
                combo = comboRepository.findById(request.getComboId())
                        .orElseThrow(() -> new IllegalArgumentException(
                                "Không tìm thấy combo với ID: " + request.getComboId()));
                if (!Boolean.TRUE.equals(combo.getIsActive())) {
                    throw new IllegalArgumentException(
                            "Combo '" + combo.getName() + "' đang ngừng kinh doanh");
                }
                // Kiểm tra các sản phẩm trong combo
                if (combo.getComboItems() != null) {
                    for (fpt.legendcoffee.entity.ComboItem comboItem : combo.getComboItems()) {
                        ProductVariant v = comboItem.getVariant();
                        if (v != null) {
                            if (!Boolean.TRUE.equals(v.getIsActive()) || 
                                (v.getProduct() != null && !Boolean.TRUE.equals(v.getProduct().getIsActive()))) {
                                throw new IllegalArgumentException(
                                    "Combo '" + combo.getName() + "' chứa sản phẩm '" + v.getVariantName() + "' đã ngừng kinh doanh");
                            }
                        }
                    }
                }
                LocalDateTime now = LocalDateTime.now();
                if (combo.getStartDate() != null && now.isBefore(combo.getStartDate())) {
                    throw new IllegalArgumentException(
                            "Combo với ID " + request.getComboId() + " chưa đến thời gian áp dụng");
                }
                if (combo.getEndDate() != null && now.isAfter(combo.getEndDate())) {
                    throw new IllegalArgumentException(
                            "Combo với ID " + request.getComboId() + " đã hết thời gian áp dụng");
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
                    .totalAmount(subTotal)
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
}
