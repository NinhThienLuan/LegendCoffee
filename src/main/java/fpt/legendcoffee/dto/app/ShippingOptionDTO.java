package fpt.legendcoffee.dto.app;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Data
@Builder
public class ShippingOptionDTO {
    private Integer serviceId;
    private String serviceName; // "Giao nhanh", "Giao chuẩn", "Tiết kiệm"
    private Long shippingFee; // VNĐ
    private LocalDateTime leadtime; // thời gian giao dự kiến

    public String getFormattedFee() {
        if (shippingFee == null)
            return "0đ";
        return String.format("%,dđ", shippingFee).replace(",", ".");
    }

    public String getFormattedLeadtime() {
        if (leadtime == null)
            return "Đang cập nhật";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEEE, dd/MM/yyyy", new java.util.Locale("vi", "VN"));
        return leadtime.format(formatter);
    }

}
