package fpt.legendcoffee.common.config;

public class SecurityConstants {
    public static final String[] PUBLIC_MATCHERS = {
            "/",
            "/home",
            "/login",
            "/register",
            "/cart",
            "/checkout",                   // Page checkout
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
            "/error",
            "/assets/**",
            "/favicon.ico"
    };
}
