package fpt.legendcoffee.dto.app;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CheckoutRequestDTO {
    // Thông tin người nhận
    @NotBlank(message = "Vui lòng nhập họ tên")
    private String recipientName;

    @NotBlank(message = "Vui lòng nhập số điện thoại")
    @Pattern(regexp = "^[0-9]{10,11}$", message = "Số điện thoại không hợp lệ")
    private String recipientPhone;

    @NotBlank(message = "Vui lòng nhập địa chỉ")
    private String recipientAddress;

    @NotNull(message = "Vui lòng chọn tỉnh/thành phố")
    private Integer provinceId;
    private String provinceName;

    @NotNull(message = "Vui lòng chọn quận/huyện")
    private Integer districtId;
    private String districtName;

    @NotBlank(message = "Vui lòng chọn phường/xã")
    private String wardCode;
    private String wardName;

    // Dịch vụ vận chuyển
    @NotNull(message = "Vui lòng chọn dịch vụ vận chuyển")
    private Integer serviceId;
    private String serviceName;
    private Long shippingFee;

    // Phương thức thanh toán
    // paymentTypeId: 1 = shop trả phí ship, 2 = người nhận trả (COD)
    @NotNull
    private Integer paymentTypeId;

    // Ghi chú đơn hàng
    private String note;

    // ID đơn hàng trong hệ thống (đã tạo trước khi gọi GHN)
    private Long orderId;

}
