package fpt.legendcoffee.dto.app;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class OrderStatusDTO {

    private String ghnOrderCode;
    private String status;
    private String statusLabel;     // nhãn tiếng Việt
    private String toName;
    private String toPhone;
    private String toAddress;
    private String finishDate;
    private List<LogItemDTO> logs;

    @Data
    @Builder
    public static class LogItemDTO {
        private String status;
        private String statusLabel;
        private String time;
    }

    /**
     * Map GHN status code sang nhãn tiếng Việt
     */
    public static String mapStatusLabel(String status) {
        return switch (status) {
            case "ready_to_pick"   -> "Chờ lấy hàng";
            case "picking"         -> "Đang lấy hàng";
            case "picked"          -> "Đã lấy hàng";
            case "storing"         -> "Đang lưu kho";
            case "transporting"    -> "Đang vận chuyển";
            case "sorting"         -> "Đang phân loại";
            case "delivering"      -> "Đang giao hàng";
            case "delivered"       -> "Giao hàng thành công";
            case "delivery_fail"   -> "Giao hàng thất bại";
            case "return"          -> "Đang hoàn hàng";
            case "returned"        -> "Đã hoàn hàng";
            case "cancel"          -> "Đã huỷ";
            case "pending"         -> "Chờ xử lý";
            case "pending_payment" -> "Chờ thanh toán";
            default                -> status;
        };
    }
}
