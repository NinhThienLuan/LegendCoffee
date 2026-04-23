package fpt.legendcoffee.common.config;

public class SecurityConstants {
    public static final String[] PUBLIC_MATCHERS = {
            "/",
            "/home",
            "/login",
            "/register",
            "/cart",
            "/api/shipping/**",
            "/webhook/ghn",
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
