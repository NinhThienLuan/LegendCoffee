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
            "/api/shipping/**",            // REST API tính phí & địa chỉ (gọi AJAX từ checkout)
            "/webhook/ghn",               // Webhook GHN — phải public để GHN gọi được
            "/articles/**",
            "/products/**",
            "/combos",
            "/combos/**",
            "/forgot-password",
            "/reset-password",
            "/css/**",
            "/js/**",
            "/images/**",
            "/favicon.ico",
            "/payment/vnpay/callback",
            "/payment/vnpay/ipn",
            "/error",
            "/assets/**",
            "/images/**"
    };
}
