package fpt.legendcoffee.service.serviceImpl;

import fpt.legendcoffee.dto.app.CheckoutRequestDTO;
import fpt.legendcoffee.entity.Order;
import fpt.legendcoffee.entity.enumeration.OrderStatus;
import fpt.legendcoffee.repository.OrderRepository;
import fpt.legendcoffee.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;

    @Override
    @Transactional
    public Order createOrder(CheckoutRequestDTO checkout) {
        log.info("[OrderService] Creating new order for recipient: {}", checkout.getRecipientName());

        // 1. Khởi tạo thực thể Order
        Order order = Order.builder()
                .orderDate(LocalDateTime.now())
                .subTotal(BigDecimal.ZERO)          // TODO: Thay bằng subTotal từ giỏ hàng thực tế
                .discount(BigDecimal.ZERO)          // TODO: Thay bằng mã giảm giá nếu có
                // Tổng đơn hàng (Tạm thời là phí ship, sẽ cộng thêm subTotal sau khi tích hợp giỏ hàng)
                .totalAmount(BigDecimal.valueOf(checkout.getShippingFee() != null ? checkout.getShippingFee() : 0))
                .status(OrderStatus.PENDING)
                .build();

        // 2. Khởi tạo thực thể ShippingInfo dựa trên thông tin địa chỉ từ DTO
        ShippingInfo shippingInfo = ShippingInfo.builder()
                .order(order)
                .recipientName(checkout.getRecipientName())
                .recipientPhone(checkout.getRecipientPhone())
                .recipientAddress(checkout.getRecipientAddress())
                .provinceId(checkout.getProvinceId())
                .provinceName(checkout.getProvinceName())
                .districtId(checkout.getDistrictId())
                .districtName(checkout.getDistrictName())
                .wardCode(checkout.getWardCode())
                .wardName(checkout.getWardName())
                .serviceId(checkout.getServiceId())
                .serviceName(checkout.getServiceName())
                .shippingFee(checkout.getShippingFee())
                .paymentTypeId(checkout.getPaymentTypeId())
                .status("ready_to_pick") // Trạng thái khởi tạo phía vận chuyển
                .note(checkout.getNote())
                .build();

        // Gán ngược lại cho order (Quan hệ One-to-One bidirectional)
        order.setShippingInfo(shippingInfo);

        // 3. Lưu xuống DB - Hibernate sẽ cascade lưu luôn shippingInfo
        Order savedOrder = orderRepository.save(order);

        log.info("[OrderService] Đã tạo đơn hàng thành công tại: {}, {}, {}",
                checkout.getWardName(), checkout.getDistrictName(), checkout.getProvinceName());
        log.info("[OrderService] Created order successfully with ID: {}", savedOrder.getId());

        return savedOrder;
    }
}
