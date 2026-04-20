package fpt.legendcoffee.common.dataInit;

import fpt.legendcoffee.entity.*;
import fpt.legendcoffee.entity.enumeration.OrderStatus;
import fpt.legendcoffee.entity.enumeration.UserRole;
import fpt.legendcoffee.repository.*;
import lombok.AllArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Component
@AllArgsConstructor
public class DataInit implements CommandLineRunner {

    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final ProductVariantRepository productVariantRepository;
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
                    .totalAmount(new BigDecimal("150000")) // 150.000 VND
                    .status(OrderStatus.PENDING)
                    .build();
            orderRepository.save(order1);

            // Order #2 — PENDING, amount lớn hơn để test case khác
            Order order2 = Order.builder()
                    .user(user)
                    .orderDate(LocalDateTime.now().minusHours(1))
                    .subTotal(new BigDecimal("320000"))
                    .discount(new BigDecimal("20000"))
                    .totalAmount(new BigDecimal("300000")) // 300.000 VND
                    .status(OrderStatus.PENDING)
                    .build();
            orderRepository.save(order2);

            System.out.println("  [DataInit] Seed 2 orders thành công");
            System.out.println("   → Order #" + order1.getId() + " — 150.000 VND (PENDING)");
            System.out.println("   → Order #" + order2.getId() + " — 300.000 VND (PENDING)");
        }

        // ===== Categories, Products & Variants =====
        if (categoryRepository.count() == 0) {
            Category hat = Category.builder()
                    .categoryName("Cà phê hạt")
                    .description("Các loại cà phê hạt nguyên chất Robusta, Arabica...")
                    .build();
            categoryRepository.save(hat);

            Category bot = Category.builder()
                    .categoryName("Cà phê bột")
                    .description("Cà phê rang xay sẵn đóng túi")
                    .build();
            categoryRepository.save(bot);

            Category dungCu = Category.builder()
                    .categoryName("Dụng cụ pha chế")
                    .description("Phin, máy pha cà phê, giấy lọc...")
                    .build();
            categoryRepository.save(dungCu);

            // Seed Products
            Product p1 = Product.builder()
                    .name("Cà phê Robusta Nguyên Chất")
                    .category(hat)
                    .origin("Lâm Đồng")
                    .description("Cà phê Robusta đậm đà, hậu vị ngọt, phù hợp pha phin.")
                    .manufacturerDate(LocalDate.now().minusMonths(1))
                    .expiryDate(LocalDate.now().plusMonths(11))
                    .isActive(true)
                    .build();
            productRepository.save(p1);

            Product p2 = Product.builder()
                    .name("Cà phê Arabica Cầu Đất")
                    .category(hat)
                    .origin("Cầu Đất, Đà Lạt")
                    .description("Cà phê Arabica thơm nhẹ, vị chua thanh, chuẩn gu thượng hạng.")
                    .manufacturerDate(LocalDate.now().minusDays(15))
                    .expiryDate(LocalDate.now().plusMonths(11).plusDays(15))
                    .isActive(true)
                    .build();
            productRepository.save(p2);

            Product p3 = Product.builder()
                    .name("Phin Pha Cà Phê Inox")
                    .category(dungCu)
                    .origin("Việt Nam")
                    .description("Phin inox cao cấp, lọc chậm, giữ trọn hương vị cà phê.")
                    .isActive(true)
                    .build();
            productRepository.save(p3);

            // Seed Variants
            ProductVariant v1_1 = ProductVariant.builder()
                    .product(p1)
                    .variantName("Bao 1kg - Hạt")
                    .packaging("Bao")
                    .size(1000)
                    .price(new BigDecimal("210000"))
                    .stockQuantity(100)
                    .isActive(true)
                    .build();
            productVariantRepository.save(v1_1);

            ProductVariant v1_2 = ProductVariant.builder()
                    .product(p1)
                    .variantName("Túi 500g - Hạt")
                    .packaging("Túi")
                    .size(500)
                    .price(new BigDecimal("110000"))
                    .stockQuantity(250)
                    .isActive(true)
                    .build();
            productVariantRepository.save(v1_2);

            ProductVariant v2_1 = ProductVariant.builder()
                    .product(p2)
                    .variantName("Gói 250g - Hạt")
                    .packaging("Gói")
                    .size(250)
                    .price(new BigDecimal("145000"))
                    .stockQuantity(150)
                    .isActive(true)
                    .build();
            productVariantRepository.save(v2_1);

            ProductVariant v3_1 = ProductVariant.builder()
                    .product(p3)
                    .variantName("Size M")
                    .packaging("Hộp")
                    .price(new BigDecimal("45000"))
                    .stockQuantity(300)
                    .isActive(true)
                    .build();
            productVariantRepository.save(v3_1);

            System.out.println("  [DataInit] Seed Categories, Products & Variants thành công");
        }
    }
}
