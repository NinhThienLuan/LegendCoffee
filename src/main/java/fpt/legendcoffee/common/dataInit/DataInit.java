package fpt.legendcoffee.common.dataInit;

import fpt.legendcoffee.entity.*;
import fpt.legendcoffee.entity.enumeration.ArticleStatus;
import fpt.legendcoffee.entity.enumeration.UserRole;
import fpt.legendcoffee.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInit implements CommandLineRunner {

    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final ProductVariantRepository productVariantRepository;
    private final ArticleRepository articleRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        log.info("Starting data initialization...");

        seedUsers();
        seedData();

        log.info("Data initialization completed.");
    }

    private void seedUsers() {
        if (userRepository.count() == 0) {
            log.info("Seeding users...");
            
            User admin = User.builder()
                    .username("Admin Legend")
                    .email("admin@legendcoffee.com")
                    .password(passwordEncoder.encode("admin123"))
                    .phone("0123456789")
                    .role(UserRole.ADMIN)
                    .isActive(true)
                    .build();

            User staff = User.builder()
                    .username("Staff Member")
                    .email("staff@legendcoffee.com")
                    .password(passwordEncoder.encode("staff123"))
                    .phone("0987654321")
                    .role(UserRole.STAFF)
                    .isActive(true)
                    .build();

            User customer = User.builder()
                    .username("John Doe")
                    .email("user@gmail.com")
                    .password(passwordEncoder.encode("user123"))
                    .phone("0333444555")
                    .role(UserRole.USER)
                    .isActive(true)
                    .build();

            userRepository.saveAll(List.of(admin, staff, customer));
        }
    }

    private void seedData() {
        if (categoryRepository.count() == 0) {
            log.info("Seeding categories, products, and articles...");

            // 1. Categories
            Category beans = Category.builder()
                    .categoryName("Hạt cà phê")
                    .description("Cà phê nguyên hạt rang xay")
                    .build();

            Category ground = Category.builder()
                    .categoryName("Cà phê bột")
                    .description("Cà phê đã xay sẵn, tiện dụng")
                    .build();

            Category instant = Category.builder()
                    .categoryName("Cà phê hòa tan")
                    .description("Cà phê uống liền nhanh chóng")
                    .build();

            categoryRepository.saveAll(List.of(beans, ground, instant));

            // 2. Products & Variants for Beans
            Product arabica = Product.builder()
                    .category(beans)
                    .name("Legend Arabica Special")
                    .description("Hạt Arabica từ vùng cầu đất Đà Lạt, hương thơm nhẹ nhàng, vị chua thanh.")
                    .origin("Đà Lạt, Việt Nam")
                    .expiryDate("12 tháng")
                    .manufacturerDate("01/04/2026")
                    .imageUrl("/images/arabica.png")
                    .isActive(true)
                    .build();
            productRepository.save(arabica);

            ProductVariant arabica250 = ProductVariant.builder()
                    .product(arabica)
                    .variantName("Túi 250g")
                    .packaging("Túi giấy Kraft")
                    .size(250)
                    .price(new BigDecimal("150000"))
                    .stockQuantity(100)
                    .isActive(true)
                    .build();

            ProductVariant arabica500 = ProductVariant.builder()
                    .product(arabica)
                    .variantName("Túi 500g")
                    .packaging("Túi giấy Kraft")
                    .size(500)
                    .price(new BigDecimal("280000"))
                    .stockQuantity(50)
                    .isActive(true)
                    .build();
            productVariantRepository.saveAll(List.of(arabica250, arabica500));

            Product robusta = Product.builder()
                    .category(beans)
                    .name("Legend Robusta Bold")
                    .description("Hạt Robusta Buôn Ma Thuột rang đậm, vị đắng mạnh mẽ, hậu vị ngọt.")
                    .origin("Đắk Lắk, Việt Nam")
                    .expiryDate("12 tháng")
                    .manufacturerDate("10/04/2026")
                    .imageUrl("/images/robusta.png")
                    .isActive(true)
                    .build();
            productRepository.save(robusta);

            ProductVariant robusta500 = ProductVariant.builder()
                    .product(robusta)
                    .variantName("Túi 500g")
                    .packaging("Túi nhôm")
                    .size(500)
                    .price(new BigDecimal("220000"))
                    .stockQuantity(200)
                    .isActive(true)
                    .build();
            productVariantRepository.save(robusta500);

            // 3. Products for Ground Coffee
            Product espressoGround = Product.builder()
                    .category(ground)
                    .name("Espresso Premium Blend (Xay)")
                    .description("Sự kết hợp hoàn hảo giữa Arabica và Robusta theo tỷ lệ 7:3.")
                    .origin("Lâm Đồng, Việt Nam")
                    .expiryDate("6 tháng")
                    .manufacturerDate("15/04/2026")
                    .imageUrl("/images/ground.png")
                    .isActive(true)
                    .build();
            productRepository.save(espressoGround);

            ProductVariant ground200 = ProductVariant.builder()
                    .product(espressoGround)
                    .variantName("Hộp 200g")
                    .packaging("Hộp thiếc")
                    .size(200)
                    .price(new BigDecimal("185000"))
                    .stockQuantity(75)
                    .isActive(true)
                    .build();
            productVariantRepository.save(ground200);

            // 4. Articles
            User admin = userRepository.findByEmail("admin@legendcoffee.com").orElse(null);
            Article article1 = Article.builder()
                    .user(admin)
                    .title("Cách pha cà phê Pour-over chuẩn vị tại nhà")
                    .summary("Khám phá kỹ thuật pha Pour-over để tận hưởng trọn vẹn hương vị của hạt Arabica.")
                    .contentJson("{\"content\": [{\"type\": \"paragraph\", \"text\": \"Dụng cụ cần thiết: Phễu lọc, giấy lọc, bình đựng, cân điện tử...\"}]}")
                    .coverImageUrl("/images/pourover.png")
                    .status(ArticleStatus.PUBLISHED)
                    .publishedAt(LocalDateTime.now())
                    .isActive(true)
                    .build();

            Article article2 = Article.builder()
                    .user(admin)
                    .title("Lợi ích của cà phê đối với sức khỏe")
                    .summary("Nhiều nghiên cứu cho thấy uống cà phê điều độ giúp cải thiện sự tập trung và tốt cho tim mạch.")
                    .contentJson("{\"content\": [{\"type\": \"paragraph\", \"text\": \"Caffein giúp tăng cường trao đổi chất và bảo vệ gan...\"}]}")
                    .coverImageUrl("/images/health.png")
                    .status(ArticleStatus.PUBLISHED)
                    .publishedAt(LocalDateTime.now().minusDays(1))
                    .isActive(true)
                    .build();

            articleRepository.saveAll(List.of(article1, article2));
        }
    }
}
