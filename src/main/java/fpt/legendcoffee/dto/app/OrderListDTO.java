package fpt.legendcoffee.dto.app;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO dùng để hiển thị danh sách đơn hàng trên trang order.html
 */
@Data
@Builder
public class OrderListDTO {
    private Long id;
    private LocalDateTime orderDate;
    private BigDecimal totalAmount;

    // Thông tin sản phẩm đầu tiên trong đơn
    private String firstItemName;
    private String firstItemDetail;
    private String firstItemImage;

    // Logic cho stack ảnh
    private java.util.List<String> itemImages;
    private int additionalItemsCount;

    // Trạng thái shipping (từ ShippingInfo)
    private String shippingStatus; // ready_to_pick, delivering, delivered, cancel...
    private String shippingStatusLabel; // Chờ lấy hàng, Đang giao hàng...
    private String paymentUrl;
}
