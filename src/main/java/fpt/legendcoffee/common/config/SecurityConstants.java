package fpt.legendcoffee.common.config;

public class SecurityConstants {
    public static final String[] PUBLIC_MATCHERS = {
            "/",
            "/home",
            "/login",
            "/register",
            "/cart",
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