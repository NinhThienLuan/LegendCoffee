package fpt.legendcoffee.common.config;

public class SecurityConstants {
    public static final String[] PUBLIC_MATCHERS = {
            "/",
            "/home",
            "/login",
            "/register",
            "/css/**",
            "/js/**",
            "/images/**",
 
            "/favicon.ico",
            "/payment/vnpay/callback",
            "/payment/vnpay/ipn",
            "/assets/**",
         
    };
}
