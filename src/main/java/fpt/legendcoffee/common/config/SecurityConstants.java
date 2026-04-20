package fpt.legendcoffee.common.config;

public class SecurityConstants {
    public static final String[] PUBLIC_MATCHERS = {
            "/",
            "/home",
            "/login",
            "/register",
            "/cart",
            "/catalogs/**",
            "/css/**",
            "/js/**",
            "/images/**",
<<<<<<< HEAD
            "/payment/vnpay/callback",
            "/payment/vnpay/ipn",
            "/assets/**",
            "/favicon.ico"
=======
 
            "/favicon.ico",
            "/payment/vnpay/callback",
            "/payment/vnpay/ipn",
            "/assets/**",
         
>>>>>>> feature/product-service
    };
}
