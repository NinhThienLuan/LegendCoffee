package fpt.legendcoffee.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import fpt.legendcoffee.common.infrastructure.BaseEntity;
import fpt.legendcoffee.entity.enumeration.ShippingStatus;

import java.time.LocalDateTime;

/**
 * Lưu thông tin vận chuyển GHN liên kết với đơn hàng.
 * Thêm bảng này vào DB của bạn.
 *
 * SQL Server script:
 * CREATE TABLE shipping_info (
 * id BIGINT IDENTITY(1,1) PRIMARY KEY,
 * order_id BIGINT NOT NULL UNIQUE,
 * ghn_order_code NVARCHAR(50),
 * recipient_name NVARCHAR(255) NOT NULL,
 * recipient_phone NVARCHAR(20) NOT NULL,
 * recipient_address NVARCHAR(500) NOT NULL,
 * province_id INT,
 * province_name NVARCHAR(100),
 * district_id INT NOT NULL,
 * district_name NVARCHAR(100),
 * ward_code NVARCHAR(20) NOT NULL,
 * ward_name NVARCHAR(100),
 * service_id INT,
 * service_name NVARCHAR(100),
 * shipping_fee BIGINT,
 * cod_amount BIGINT,
 * payment_type_id INT DEFAULT 1,
 * status NVARCHAR(50) DEFAULT 'ready_to_pick',
 * expected_delivery_time DATETIME,
 * note NVARCHAR(500),
 * created_at DATETIME DEFAULT GETDATE(),
 * updated_at DATETIME DEFAULT GETDATE(),
 * FOREIGN KEY (order_id) REFERENCES orders(id)
 * );
 */
@Entity
@EqualsAndHashCode(callSuper = true)
@Table(name = "shipping_info")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class ShippingInfo extends BaseEntity {

    @OneToOne
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Column(name = "ghn_order_code", length = 50)
    private String ghnOrderCode;

    @Column(name = "recipient_name", nullable = false, columnDefinition = "NVARCHAR(255)")
    private String recipientName;

    @Column(name = "recipient_phone", nullable = false, length = 20)
    private String recipientPhone;

    @Column(name = "recipient_address", nullable = false, columnDefinition = "NVARCHAR(500)")
    private String recipientAddress;

    @Column(name = "province_id")
    private Integer provinceId;

    @Column(name = "province_name", columnDefinition = "NVARCHAR(100)")
    private String provinceName;

    @Column(name = "district_id", nullable = false)
    private Integer districtId;

    @Column(name = "district_name", columnDefinition = "NVARCHAR(100)")
    private String districtName;

    @Column(name = "ward_code", nullable = false, length = 20)
    private String wardCode;

    @Column(name = "ward_name", columnDefinition = "NVARCHAR(100)")
    private String wardName;

    // Thông tin dịch vụ vận chuyển
    @Column(name = "service_id")
    private Integer serviceId;

    @Column(name = "service_name", length = 100)
    private String serviceName;

    @Column(name = "shipping_fee")
    private Long shippingFee;

    // Tiền COD (nếu payment_type_id = 2)
    @Column(name = "cod_amount")
    private Long codAmount;

    // 1 = shop trả phí, 2 = người nhận trả (COD)
    @Column(name = "payment_type_id")
    private Integer paymentTypeId;

    // Trạng thái GHN: ready_to_pick, delivering, delivered, cancel, ...
    @Column(name = "status", length = 50)
    private ShippingStatus status;

    @Column(name = "expected_delivery_time")
    private LocalDateTime expectedDeliveryTime;

    @Column(name = "note", columnDefinition = "NVARCHAR(500)")
    private String note;
}
