package fpt.legendcoffee.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // CSRF protection is enabled by default in Spring Security 6
                .csrf(csrf -> {}) 

                .authorizeHttpRequests(auth -> auth
                        // Public endpoints
                        .requestMatchers(HttpMethod.POST, "/forgot-password", "/reset-password").permitAll()
                        .requestMatchers(SecurityConstants.PUBLIC_MATCHERS).permitAll()
                        // Admin-only section
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        // Customer-only pages — ADMIN is blocked from these
                        .requestMatchers("/cart/**", "/checkout/**", "/payment/**").hasRole("USER")
                        .anyRequest().authenticated())
                .securityContext(context -> context
                        .securityContextRepository(securityContextRepository()))
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint((request, response, authException) -> {
                            System.out.println("Authentication failed: " + authException.getMessage());
                            response.sendRedirect("/login");
                        })
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            // Redirect ADMIN away from customer pages → admin dashboard
                            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
                            if (auth != null && auth.getAuthorities().stream()
                                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
                                response.sendRedirect("/admin");
                            } else {
                                response.sendRedirect("/home");
                            }
                        }))
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout=true")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID"));
        return http.build();
    }

    @Bean
    public SecurityContextRepository securityContextRepository() {
        return new HttpSessionSecurityContextRepository();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}