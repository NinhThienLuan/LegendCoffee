package fpt.legendcoffee.common.config;

public class SecurityConstants {
    public static final String[] PUBLIC_MATCHERS = {
            "/",
            "/home",
            "/login",
            "/register",
            "/catalogs",
            "/css/**",
            "/js/**",
            "/images/**",
            "/assets/**",
            "/favicon.ico"
    };
}
