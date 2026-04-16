package fpt.legendcoffee.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Response trả về cho VNPay server khi nhận IPN.
 * <p>
 * VNPay yêu cầu JSON format: {"RspCode": "00", "Message": "..."}<br>
 * Phải dùng @JsonProperty để đảm bảo đúng tên key (chữ hoa đầu).
 */
public class VNPayIpnResponseDTO {

    @JsonProperty("RspCode")
    private String rspCode;

    @JsonProperty("Message")
    private String message;

    public VNPayIpnResponseDTO() {}

    public VNPayIpnResponseDTO(String rspCode, String message) {
        this.rspCode = rspCode;
        this.message = message;
    }

    public String getRspCode() { return rspCode; }
    public void setRspCode(String rspCode) { this.rspCode = rspCode; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    // ===== Static factory methods =====

    /** VNPay đã nhận IPN thành công (RspCode 00) */
    public static VNPayIpnResponseDTO confirmSuccess() {
        return new VNPayIpnResponseDTO("00", "Confirm Success");
    }

    /** Chữ ký không hợp lệ (RspCode 97) */
    public static VNPayIpnResponseDTO invalidSignature() {
        return new VNPayIpnResponseDTO("97", "Invalid signature");
    }

    /** Đơn hàng không tồn tại (RspCode 01) */
    public static VNPayIpnResponseDTO orderNotFound() {
        return new VNPayIpnResponseDTO("01", "Order not found");
    }

    /** Số tiền không khớp (RspCode 04) */
    public static VNPayIpnResponseDTO invalidAmount() {
        return new VNPayIpnResponseDTO("04", "Invalid amount");
    }
}
