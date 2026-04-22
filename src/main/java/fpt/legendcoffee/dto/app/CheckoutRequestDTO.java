package fpt.legendcoffee.dto.app;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CheckoutRequestDTO {
    // Thông tin người nhận
    @NotBlank(message = "Vui lòng nhập họ tên")
    private String recipientName;

    @NotBlank(message = "Số điện thoại không được để trống")
    @Pattern(regexp = "^(0|84)(3|5|7|8|9)([0-9]{8})$", message = "Số điện thoại Việt Nam không hợp lệ (ví dụ: 0912345678)")
    private String recipientPhone;

    @NotBlank(message = "Địa chỉ chi tiết không được để trống")
    private String recipientAddress;

    @NotNull(message = "Vui lòng chọn Tỉnh/Thành phố")
    private Integer provinceId;
    private String provinceName;

    @NotNull(message = "Vui lòng chọn Quận/Huyện")
    private Integer districtId;
    private String districtName;

    @NotBlank(message = "Vui lòng chọn Phường/Xã")
    private String wardCode;
    private String wardName;

    // Dịch vụ vận chuyển
    @NotNull(message = "Vui lòng chọn một dịch vụ vận chuyển khả dụng")
    private Integer serviceId;
    private String serviceName;
    private Long shippingFee;

    // Phương thức thanh toán
    @NotNull(message = "Vui lòng chọn phương thức thanh toán")
    private Integer paymentTypeId;

    // Ghi chú đơn hàng
    private String note;

    // ID đơn hàng trong hệ thống (đã tạo trước khi gọi GHN)
    private Long orderId;

    // Danh sách item checkout; mỗi item chỉ được có variantId hoặc comboId.
    @Valid
    @Builder.Default
    private List<CheckoutItemRequestDTO> items = new ArrayList<>();

}
