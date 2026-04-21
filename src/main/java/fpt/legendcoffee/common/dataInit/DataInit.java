package fpt.legendcoffee.common.dataInit;

import fpt.legendcoffee.entity.*;
import fpt.legendcoffee.entity.enumeration.ArticleStatus;
import fpt.legendcoffee.entity.enumeration.OrderStatus;
import fpt.legendcoffee.entity.enumeration.UserRole;
import fpt.legendcoffee.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInit implements CommandLineRunner {

    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final ProductVariantRepository productVariantRepository;
    private final ComboRepository comboRepository;
    private final ArticleRepository articleRepository;
    private final CategoryRepository categoryRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(@NonNull String... args) {
        log.info("Starting data initialization...");

        seedUsers();
        seedData();
        seedCombos();

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

            User customer = User.builder()
                    .username("John Doe")
                    .email("user@gmail.com")
                    .password(passwordEncoder.encode("user123"))
                    .phone("0333444555")
                    .role(UserRole.USER)
                    .isActive(true)
                    .build();

            userRepository.saveAll(List.of(admin, customer));
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
                    .expiryDate(LocalDate.now().plusMonths(12))
                    .manufacturerDate(LocalDate.now())
                    .imageUrl("https://res.cloudinary.com/myimagename/image/upload/q_auto/f_auto/v1776703528/Gemini_Generated_Image_ymu9s0ymu9s0ymu9_jqlxbd.png")
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
                    .expiryDate(LocalDate.now().plusMonths(12))
                    .manufacturerDate(LocalDate.now())
                    .imageUrl("https://res.cloudinary.com/myimagename/image/upload/q_auto/f_auto/v1776704007/39e9e7ac-801d-4380-80c0-a6aeabd3590e_s8s2jh.jpg")
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
                    .expiryDate(LocalDate.now().plusMonths(12))
                    .manufacturerDate(LocalDate.now())
                    .imageUrl("https://res.cloudinary.com/myimagename/image/upload/q_auto/f_auto/v1776704017/3530370d-f499-49b9-949c-f8ca3780dfa9_xmutfc.jpg")
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
                    .title("Tối ưu hóa chuỗi cung ứng cà phê trong kỷ nguyên số")
                    .summary("Bài viết mô phỏng dữ liệu từ model để render nội dung động theo format Editor.js.")
                    .contentJson("""
                                {
                                    "time": 1713550000000,
                                    "version": "2.29.1",
                                    "blocks": [
                                        {
                                            "type": "header",
                                            "data": {
                                                "text": "Tư duy vận hành hiện đại cho ngành cà phê",
                                                "level": 2
                                            }
                                        },
                                        {
                                            "type": "paragraph",
                                            "data": {
                                                "text": "Doanh nghiệp B2B cần kết nối rang xay, kho vận và dữ liệu thời gian thực để giảm rủi ro và tăng hiệu suất."
                                            }
                                        },
                                        {
                                            "type": "image",
                                            "data": {
                                                "url": "https://images.unsplash.com/photo-1511920170033-f8396924c348?auto=format&fit=crop&w=1200&q=80",
                                                "caption": "Theo dõi chất lượng hạt và dữ liệu vận hành theo thời gian thực"
                                            }
                                        },
                                        {
                                            "type": "list",
                                            "data": {
                                                "style": "unordered",
                                                "items": [
                                                    "Theo dõi tồn kho theo lô hàng",
                                                    "Chuẩn hóa chất lượng theo profile rang",
                                                    "Tối ưu chi phí logistics liên vùng"
                                                ]
                                            }
                                        },
                                        {
                                            "type": "quote",
                                            "data": {
                                                "text": "Dữ liệu tốt giúp quyết định nhanh và đúng trong chuỗi cung ứng.",
                                                "caption": "RoastLogistics Insight"
                                            }
                                        }
                                    ]
                                }
                                """)
                    .coverImageUrl("https://res.cloudinary.com/myimagename/image/upload/q_auto/f_auto/v1776704026/45933f08-70d1-4bbf-805f-54690cd822ef_rhtyhx.jpg")
                    .status(ArticleStatus.PUBLISHED)
                    .publishedAt(LocalDateTime.now())
                    .isActive(true)
                    .build();

            User customer = userRepository.findByEmail("user@gmail.com").orElse(null);
            // Order #2 — PENDING, amount lớn hơn để test case khác
            Order order2 = Order.builder()
                    .user(customer)
                    .orderDate(LocalDateTime.now().minusHours(1))
                    .subTotal(new BigDecimal("320000"))
                    .discount(new BigDecimal("20000"))
                    .totalAmount(new BigDecimal("300000")) // 300.000 VND
                    .status(OrderStatus.PENDING)
                    .build();
            orderRepository.save(order2);

            articleRepository.saveAll(List.of(article1));
        }
    }

    private void seedCombos() {
        if (comboRepository.count() > 0 || productVariantRepository.count() == 0) {
            return;
        }

        ProductVariant arabica250 = productVariantRepository.findAll().stream()
                .filter(variant -> variant.getProduct() != null
                        && "Legend Arabica Special".equals(variant.getProduct().getName())
                        && "Túi 250g".equals(variant.getVariantName()))
                .findFirst()
                .orElse(null);

        ProductVariant robusta500 = productVariantRepository.findAll().stream()
                .filter(variant -> variant.getProduct() != null
                        && "Legend Robusta Bold".equals(variant.getProduct().getName())
                        && "Túi 500g".equals(variant.getVariantName()))
                .findFirst()
                .orElse(null);

        ProductVariant ground200 = productVariantRepository.findAll().stream()
                .filter(variant -> variant.getProduct() != null
                        && "Espresso Premium Blend (Xay)".equals(variant.getProduct().getName())
                        && "Hộp 200g".equals(variant.getVariantName()))
                .findFirst()
                .orElse(null);

        if (arabica250 == null || robusta500 == null || ground200 == null) {
            log.warn("Skipping combo seed because one or more required variants were not found.");
            return;
        }

        Combo discoveryCombo = Combo.builder()
                .name("Combo Discovery 3 vị")
                .description("Bộ thử vị gồm 3 dòng sản phẩm chủ lực với giá ưu đãi.")
                .price(new BigDecimal("540000"))
                .startDate(LocalDateTime.now().minusDays(7))
                .endDate(LocalDateTime.now().plusMonths(2))
                .isActive(true)
                .build();

        discoveryCombo.getComboItems().add(ComboItem.builder().combo(discoveryCombo).variant(arabica250).quantity(1).build());
        discoveryCombo.getComboItems().add(ComboItem.builder().combo(discoveryCombo).variant(robusta500).quantity(1).build());
        discoveryCombo.getComboItems().add(ComboItem.builder().combo(discoveryCombo).variant(ground200).quantity(1).build());

        comboRepository.save(discoveryCombo);
        log.info("Seeded combo data successfully.");
    }
}
