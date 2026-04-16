package fpt.legendcoffee.common.dataInit;

import fpt.legendcoffee.entity.Order;
import fpt.legendcoffee.entity.User;
import fpt.legendcoffee.entity.enumeration.OrderStatus;
import fpt.legendcoffee.entity.enumeration.UserRole;
import fpt.legendcoffee.repository.OrderRepository;
import fpt.legendcoffee.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Component
@AllArgsConstructor
public class DataInit implements CommandLineRunner {

    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        final String defaultPassword = "password123";

        // ===== Users =====

        if (userRepository.findByUsername("admin").isEmpty()) {
            User admin = User.builder()
                    .username("admin")
                    .password(passwordEncoder.encode(defaultPassword))
                    .email("admin@legendcoffee.com")
                    .phone("0123456789")
                    .address("Legend Coffee HQ")
                    .role(UserRole.ADMIN)
                    .isActive(true)
                    .build();
            userRepository.save(admin);
        }

        if (userRepository.findByUsername("user").isEmpty()) {
            User user = User.builder()
                    .username("user")
                    .password(passwordEncoder.encode(defaultPassword))
                    .email("user@gmail.com")
                    .phone("0987654321")
                    .address("123 Street")
                    .role(UserRole.USER)
                    .isActive(true)
                    .build();
            userRepository.save(user);
        }

        // ===== Seed Orders (chỉ tạo khi chưa có order nào) =====
        if (orderRepository.count() == 0) {
            User user = userRepository.findByUsername("user")
                    .orElseThrow(() -> new RuntimeException("User 'user' không tồn tại"));

            // Order #1 — PENDING, dùng để test VNPay payment
            Order order1 = Order.builder()
                    .user(user)
                    .orderDate(LocalDateTime.now())
                    .subTotal(new BigDecimal("150000"))
                    .discount(BigDecimal.ZERO)
                    .totalAmount(new BigDecimal("150000"))   // 150.000 VND
                    .status(OrderStatus.PENDING)
                    .build();
            orderRepository.save(order1);

            // Order #2 — PENDING, amount lớn hơn để test case khác
            Order order2 = Order.builder()
                    .user(user)
                    .orderDate(LocalDateTime.now().minusHours(1))
                    .subTotal(new BigDecimal("320000"))
                    .discount(new BigDecimal("20000"))
                    .totalAmount(new BigDecimal("300000"))   // 300.000 VND
                    .status(OrderStatus.PENDING)
                    .build();
            orderRepository.save(order2);

            System.out.println("  [DataInit] Seed 2 orders thành công");
            System.out.println("   → Order #" + order1.getId() + " — 150.000 VND (PENDING)");
            System.out.println("   → Order #" + order2.getId() + " — 300.000 VND (PENDING)");
        }
    }
}
