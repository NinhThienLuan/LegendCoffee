package fpt.legendcoffee.service.serviceImpl;

import fpt.legendcoffee.dto.app.CheckoutItemRequestDTO;
import fpt.legendcoffee.dto.app.CheckoutRequestDTO;
import fpt.legendcoffee.dto.app.OrderListDTO;
import fpt.legendcoffee.dto.app.OrderStatusDTO;
import fpt.legendcoffee.entity.*;
import fpt.legendcoffee.entity.enumeration.OrderStatus;
import fpt.legendcoffee.repository.*;
import fpt.legendcoffee.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final ProductVariantRepository productVariantRepository;
    private final ComboRepository comboRepository;
    private final ShippingInfoRepository shippingInfoRepository;

    @Override
    @Transactional
    public Order createOrder(CheckoutRequestDTO checkout) {
        log.info("[OrderService] Creating new order for recipient: {}", checkout.getRecipientName());

        List<OrderItem> draftItems = buildOrderItems(checkout.getItems());
        BigDecimal subTotal = draftItems.stream()
                .map(OrderItem::getSubTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal shippingFee = BigDecimal.valueOf(checkout.getShippingFee() != null ? checkout.getShippingFee() : 0);

        Order order = Order.builder()
                .orderDate(LocalDateTime.now())
                .subTotal(subTotal)
                .discount(BigDecimal.ZERO)
                .totalAmount(subTotal.add(shippingFee))
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

        log.info("[OrderService] Created order successfully with ID: {}", savedOrder.getId());
        return savedOrder;
    }

    @Override
    public List<OrderListDTO> getAllOrdersForList() {
        List<Order> orders = orderRepository.findAll();
        return orders.stream().map(order -> {
            // Lấy sản phẩm đầu tiên
            String itemName = null, itemDetail = null, itemImage = null;
            // Lấy trạng thái shipping
            ShippingInfo shipping = order.getShippingInfo();
            String shippingStatus = shipping != null ? shipping.getStatus() : "pending";

            return OrderListDTO.builder()
                    .id(order.getId())
                    .orderDate(order.getOrderDate())
                    .totalAmount(order.getTotalAmount())
                    .firstItemName(itemName)
                    .firstItemDetail(itemDetail)
                    .firstItemImage(itemImage)
                    .shippingStatus(shippingStatus)
                    .shippingStatusLabel(OrderStatusDTO.mapStatusLabel(shippingStatus))
                    .build();
        }).toList();
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

            if (hasVariant) {
                variant = productVariantRepository.findById(request.getVariantId())
                        .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy biến thể với ID: " + request.getVariantId()));
                if (!Boolean.TRUE.equals(variant.getIsActive())) {
                    throw new IllegalArgumentException("Biến thể với ID " + request.getVariantId() + " đang không hoạt động");
                }
                unitPrice = variant.getPrice();
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
                    .totalAmount(subTotal)
                    .build());
        }

        return items;
    }
}
