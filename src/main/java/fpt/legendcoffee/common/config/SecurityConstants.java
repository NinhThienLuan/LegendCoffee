package fpt.legendcoffee.common.config;

public class SecurityConstants {
    public static final String[] PUBLIC_MATCHERS = {
            "/",
            "/home",
            "/login",
            "/register",
            "/cart",
            "/checkout",                   // Trang checkout (yêu cầu đăng nhập thì xoá dòng này)
            "/checkout/place-order",       // Submit đặt hàng
            "/orders/*/track",             // Trang tracking đơn hàng
            "/api/shipping/**",            // REST API tính phí & địa chỉ (gọi AJAX từ checkout)
            "/webhook/ghn",               // Webhook GHN — phải public để GHN gọi được
            "/articles/**",
            "/products/**",
            "/css/**",
            "/js/**",
            "/images/**",
            "/payment/vnpay/callback",
            "/payment/vnpay/ipn",
            "/assets/**",
            "/favicon.ico"
    };
}
