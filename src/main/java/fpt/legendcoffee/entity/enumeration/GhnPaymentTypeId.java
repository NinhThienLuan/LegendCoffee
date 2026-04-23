package fpt.legendcoffee.entity.enumeration;

/**
 * Quy định ai là người trả phí vận chuyển cho đơn hàng GHN.
 */
public enum GhnPaymentTypeId {
    SHOP_PAYS(1),      // Người gửi/Shop trả phí (Dùng khi khách đã thanh toán trước qua VNPay, Wallet)
    CUSTOMER_PAYS(2);  // Người nhận/Khách trả phí (Dùng khi giao hàng thu hộ COD)

    private final int value;

    GhnPaymentTypeId(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
