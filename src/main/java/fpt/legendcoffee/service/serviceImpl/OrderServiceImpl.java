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

        // TODO: Tích hợp logic lấy dữ liệu giỏ hàng thực tế ở đây (như tổng tiền món hàng, user đang login).
        // Tạm thời tạo một Order cơ bản với các giá trị mặc định để hoàn thiện flow
        
        Order order = Order.builder()
                .orderDate(LocalDateTime.now())
                .subTotal(BigDecimal.ZERO)          // Thay bằng subTotal từ giỏ hàng
                .discount(BigDecimal.ZERO)          // Thay bằng mã giảm giá nếu có
                // Tổng đơn hàng (ví dụ tạm thời = Giá trị đơn hàng + Phí ship)
                // Trường hợp người nhận trả phí (COD) thì có thể tuỳ chỉnh tổng tiền.
                .totalAmount(BigDecimal.valueOf(checkout.getShippingFee() != null ? checkout.getShippingFee() : 0))  
                .status(OrderStatus.PENDING)        // Trạng thái đơn khởi tạo
                .build();

        // Lưu xuống DB - Hibernate sẽ tạo ra ID cho order
        Order savedOrder = orderRepository.save(order);
        
        log.info("[OrderService] Created order successfully with ID: {}", savedOrder.getId());
        return savedOrder;
    }
}
