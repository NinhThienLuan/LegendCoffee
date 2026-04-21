package fpt.legendcoffee.service.serviceImpl;

import fpt.legendcoffee.dto.app.CheckoutItemRequestDTO;
import fpt.legendcoffee.dto.app.CheckoutRequestDTO;
import fpt.legendcoffee.entity.Combo;
import fpt.legendcoffee.entity.Order;
import fpt.legendcoffee.entity.OrderItem;
import fpt.legendcoffee.entity.ProductVariant;
import fpt.legendcoffee.entity.enumeration.OrderStatus;
import fpt.legendcoffee.repository.ComboRepository;
import fpt.legendcoffee.repository.OrderItemRepository;
import fpt.legendcoffee.repository.OrderRepository;
import fpt.legendcoffee.repository.ProductVariantRepository;
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
