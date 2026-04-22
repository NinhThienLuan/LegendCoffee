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
        private final OrderItemRepository orderItemRepository;
        private final ProductVariantRepository productVariantRepository;
        private final ComboRepository comboRepository;
        private final ArticleRepository articleRepository;
        private final CategoryRepository categoryRepository;
        private final ShippingInfoRepository shippingInfoRepository;
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

                        Category accessories = Category.builder()
                                        .categoryName("Phụ kiện pha chế")
                                        .description("Các dụng cụ hỗ trợ pha chế cà phê")
                                        .build();

                        categoryRepository.saveAll(List.of(beans, ground, instant, accessories));

                        // 2. Products & Variants for Beans
                        Product arabica = Product.builder()
                                        .category(beans)
                                        .name("Legend Arabica Special")
                                        .description("Hạt Arabica từ vùng cầu đất Đà Lạt, hương thơm nhẹ nhàng, vị chua thanh.")
                                        .origin("Đà Lạt, Việt Nam")
                                        .expiryDate(LocalDate.now().plusMonths(12))
                                        .manufacturerDate(LocalDate.now())
                                        .imageUrl(
                                                        "https://res.cloudinary.com/myimagename/image/upload/q_auto/f_auto/v1776703528/Gemini_Generated_Image_ymu9s0ymu9s0ymu9_jqlxbd.png")
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
                                        .imageUrl(
                                                        "https://res.cloudinary.com/myimagename/image/upload/q_auto/f_auto/v1776704007/39e9e7ac-801d-4380-80c0-a6aeabd3590e_s8s2jh.jpg")
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
                                        .imageUrl(
                                                        "https://res.cloudinary.com/myimagename/image/upload/q_auto/f_auto/v1776704017/3530370d-f499-49b9-949c-f8ca3780dfa9_xmutfc.jpg")
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

                        // 3.5 Accessories
                        Product filterMaker = Product.builder()
                                        .category(accessories)
                                        .name("Phin pha cà phê inox")
                                        .description("Phin inox cao cấp, giữ nhiệt tốt.")
                                        .origin("Việt Nam")
                                        .isActive(true)
                                        .build();
                        productRepository.save(filterMaker);

                        ProductVariant filterMakerV = ProductVariant.builder()
                                        .product(filterMaker).variantName("Size Tiêu Chuẩn")
                                        .packaging("Hộp giấy").size(1)
                                        .price(new BigDecimal("85000")).stockQuantity(3).isActive(true).build();
                        productVariantRepository.save(filterMakerV);

                        Product v60 = Product.builder()
                                        .category(accessories)
                                        .name("Phễu pha V60 Hario")
                                        .description("Phễu pha cà phê pour-over bằng sứ.")
                                        .origin("Nhật Bản")
                                        .isActive(true)
                                        .build();
                        productRepository.save(v60);

                        ProductVariant v60V = ProductVariant.builder()
                                        .product(v60).variantName("Size 02")
                                        .packaging("Hộp giấy").size(1)
                                        .price(new BigDecimal("450000")).stockQuantity(2).isActive(true).build();
                        productVariantRepository.save(v60V);

                        Product scale = Product.builder()
                                        .category(accessories)
                                        .name("Cân điện tử pha chế")
                                        .description("Cân định lượng và đếm thời gian.")
                                        .origin("Trung Quốc")
                                        .isActive(true)
                                        .build();
                        productRepository.save(scale);

                        ProductVariant scaleV = ProductVariant.builder()
                                        .product(scale).variantName("Tiêu Chuẩn")
                                        .packaging("Hộp giấy").size(1)
                                        .price(new BigDecimal("350000")).stockQuantity(5).isActive(true).build();
                        productVariantRepository.save(scaleV);

                        Product grinder = Product.builder()
                                        .category(accessories)
                                        .name("Máy xay cà phê cầm tay")
                                        .description("Máy xay tay mini Timemore.")
                                        .origin("Đài Loan")
                                        .isActive(true)
                                        .build();
                        productRepository.save(grinder);

                        ProductVariant grinderV = ProductVariant.builder()
                                        .product(grinder).variantName("C3")
                                        .packaging("Hộp giấy").size(1)
                                        .price(new BigDecimal("1250000")).stockQuantity(1).isActive(true).build();
                        productVariantRepository.save(grinderV);

                        Product paperFilter = Product.builder()
                                        .category(accessories)
                                        .name("Giấy lọc V60")
                                        .description("Giấy lọc cà phê tự nhiên, không tẩy trắng.")
                                        .origin("Nhật Bản")
                                        .isActive(true)
                                        .build();
                        productRepository.save(paperFilter);

                        ProductVariant paperFilterV = ProductVariant.builder()
                                        .product(paperFilter).variantName("Hộp 100 tờ")
                                        .packaging("Hộp giấy").size(1)
                                        .price(new BigDecimal("120000")).stockQuantity(4).isActive(true).build();
                        productVariantRepository.save(paperFilterV);

                        // 4. Articles
                        User admin = userRepository.findByEmail("admin@legendcoffee.com").orElse(null);
                        Article article1 = Article.builder()
                                        .user(admin)
                                        .title("Tối ưu hóa chuỗi cung ứng cà phê trong kỷ nguyên số")
                                        .summary("Bài viết mô phỏng dữ liệu từ model để render nội dung động theo format Editor.js.")
                                        .contentJson(
                                                        """
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
                                        .coverImageUrl(
                                                        "https://res.cloudinary.com/myimagename/image/upload/q_auto/f_auto/v1776704026/45933f08-70d1-4bbf-805f-54690cd822ef_rhtyhx.jpg")
                                        .status(ArticleStatus.PUBLISHED)
                                        .publishedAt(LocalDateTime.now())
                                        .isActive(true)
                                        .build();

                        Article article2 = Article.builder()
                                        .user(admin).title("Nghệ thuật pha chế Pour Over")
                                        .summary("Bí quyết kiểm soát dòng chảy để có tách cà phê hoàn hảo.")
                                        .contentJson("{\"time\":1713550000000,\"version\":\"2.29.1\",\"blocks\":[{\"type\":\"paragraph\",\"data\":{\"text\":\"Đổ nước quá nhanh sẽ làm nhạt cà phê...\"}}]}")
                                        .coverImageUrl("https://images.unsplash.com/photo-1509042239860-f550ce710b93?q=80&w=687&auto=format&fit=crop&ixlib=rb-4.1.0&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D")
                                        .status(ArticleStatus.PUBLISHED).publishedAt(LocalDateTime.now()).isActive(true)
                                        .build();

                        Article article3 = Article.builder()
                                        .user(admin).title("Phân biệt Arabica và Robusta")
                                        .summary("Những điểm khác biệt cốt lõi giữa hai dòng hạt phổ biến nhất.")
                                        .contentJson("{\"time\":1713550000001,\"version\":\"2.29.1\",\"blocks\":[{\"type\":\"paragraph\",\"data\":{\"text\":\"Arabica có vị chua thanh, Robusta đắng đậm...\"}}]}")
                                        .coverImageUrl("https://images.unsplash.com/photo-1495474472287-4d71bcdd2085?q=80&w=1170&auto=format&fit=crop&ixlib=rb-4.1.0&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D")
                                        .status(ArticleStatus.PUBLISHED).publishedAt(LocalDateTime.now().minusDays(1))
                                        .isActive(true).build();

                        Article article4 = Article.builder()
                                        .user(admin).title("Bảo quản cà phê đúng cách")
                                        .summary("Hướng dẫn bảo quản hạt tránh mất hương vị.")
                                        .contentJson("{\"time\":1713550000002,\"version\":\"2.29.1\",\"blocks\":[{\"type\":\"paragraph\",\"data\":{\"text\":\"Tuyệt đối không để cà phê hạt vào tủ lạnh...\"}}]}")
                                        .coverImageUrl("https://images.unsplash.com/photo-1556742526-795a8eac090e?q=80&w=687&auto=format&fit=crop&ixlib=rb-4.1.0&ixid=M3wxMjA3fDF8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D")
                                        .status(ArticleStatus.PUBLISHED).publishedAt(LocalDateTime.now().minusDays(2))
                                        .isActive(true).build();

                        Article article5 = Article.builder()
                                        .user(admin).title("Cà phê Cold Brew giải nhiệt mùa hè")
                                        .summary("Công thức ngâm lạnh tại nhà cực kỳ dễ làm.")
                                        .contentJson("{\"time\":1713550000003,\"version\":\"2.29.1\",\"blocks\":[{\"type\":\"paragraph\",\"data\":{\"text\":\"Tỷ lệ 1:10 và thời gian ngâm 16-24 tiếng...\"}}]}")
                                        .coverImageUrl("https://images.unsplash.com/photo-1495774856032-8b90bbb32b32?q=80&w=1170&auto=format&fit=crop&ixlib=rb-4.1.0&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D")
                                        .status(ArticleStatus.PUBLISHED).publishedAt(LocalDateTime.now().minusDays(3))
                                        .isActive(true).build();

                        Article article6 = Article.builder()
                                        .user(admin).title("Tương lai của ngành Specialty Coffee")
                                        .summary("Xu hướng trải nghiệm cà phê đặc sản năm nay.")
                                        .contentJson("{\"time\":1713550000004,\"version\":\"2.29.1\",\"blocks\":[{\"type\":\"paragraph\",\"data\":{\"text\":\"Người dùng đang tìm kiếm những nốt hương hoa quả...\"}}]}")
                                        .coverImageUrl("https://images.unsplash.com/photo-1498804103079-a6351b050096?q=80&w=687&auto=format&fit=crop&ixlib=rb-4.1.0&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D")
                                        .status(ArticleStatus.PUBLISHED).publishedAt(LocalDateTime.now().minusDays(4))
                                        .isActive(true).build();

                        User customer = userRepository.findByEmail("user@gmail.com").orElse(null);

                        // Lấy variants đã seed để gắn vào OrderItem
                        List<ProductVariant> allVariants = productVariantRepository.findAll();
                        ProductVariant variant1 = allVariants.size() > 0 ? allVariants.get(0) : null;
                        ProductVariant variant2 = allVariants.size() > 1 ? allVariants.get(1) : null;
                        ProductVariant variant3 = allVariants.size() > 2 ? allVariants.get(2) : null;

                        // === Order 1: ready_to_pick (để test nút Admin "Giao hàng") ===
                        Order order1 = Order.builder()
                                        .user(customer)
                                        .orderDate(LocalDateTime.now().minusDays(2))
                                        .subTotal(new BigDecimal("125000000"))
                                        .discount(BigDecimal.ZERO)
                                        .totalAmount(new BigDecimal("125000000"))
                                        .status(OrderStatus.CONFIRMED)
                                        .build();
                        orderRepository.save(order1);

                        if (variant1 != null) {
                                OrderItem item1 = OrderItem.builder()
                                                .order(order1).variant(variant1).quantity(500)
                                                .unitPrice(variant1.getPrice())
                                                .subTotal(new BigDecimal("125000000"))
                                                .discount(BigDecimal.ZERO)
                                                .totalAmount(new BigDecimal("125000000"))
                                                .status("CONFIRMED")
                                                .build();
                                orderItemRepository.save(item1);
                        }

                        ShippingInfo ship1 = ShippingInfo.builder()
                                        .order(order1)
                                        .ghnOrderCode("GHN-TEST-001")
                                        .recipientName("Nguyễn Văn A")
                                        .recipientPhone("0901234567")
                                        .recipientAddress("123 Lê Lợi, Q.1, TP.HCM")
                                        .districtId(1442).wardCode("20101")
                                        .status("ready_to_pick")
                                        .build();
                        shippingInfoRepository.save(ship1);

                        // === Order 2: delivering ===
                        Order order2 = Order.builder()
                                        .user(customer)
                                        .orderDate(LocalDateTime.now().minusDays(5))
                                        .subTotal(new BigDecimal("58000000"))
                                        .discount(BigDecimal.ZERO)
                                        .totalAmount(new BigDecimal("58000000"))
                                        .status(OrderStatus.SHIPPING)
                                        .build();
                        orderRepository.save(order2);

                        if (variant2 != null) {
                                OrderItem item2 = OrderItem.builder()
                                                .order(order2).variant(variant2).quantity(200)
                                                .unitPrice(variant2.getPrice())
                                                .subTotal(new BigDecimal("58000000"))
                                                .discount(BigDecimal.ZERO)
                                                .totalAmount(new BigDecimal("58000000"))
                                                .status("SHIPPING")
                                                .build();
                                orderItemRepository.save(item2);
                        }

                        ShippingInfo ship2 = ShippingInfo.builder()
                                        .order(order2)
                                        .ghnOrderCode("GHN-TEST-002")
                                        .recipientName("Trần Thị B")
                                        .recipientPhone("0907654321")
                                        .recipientAddress("456 Nguyễn Huệ, Q.1, TP.HCM")
                                        .districtId(1442).wardCode("20102")
                                        .status("delivering")
                                        .build();
                        shippingInfoRepository.save(ship2);

                        // === Order 3: delivered ===
                        Order order3 = Order.builder()
                                        .user(customer)
                                        .orderDate(LocalDateTime.now().minusDays(10))
                                        .subTotal(new BigDecimal("192000000"))
                                        .discount(BigDecimal.ZERO)
                                        .totalAmount(new BigDecimal("192000000"))
                                        .status(OrderStatus.COMPLETED)
                                        .build();
                        orderRepository.save(order3);

                        if (variant3 != null) {
                                OrderItem item3 = OrderItem.builder()
                                                .order(order3).variant(variant3).quantity(800)
                                                .unitPrice(variant3.getPrice())
                                                .subTotal(new BigDecimal("192000000"))
                                                .discount(BigDecimal.ZERO)
                                                .totalAmount(new BigDecimal("192000000"))
                                                .status("COMPLETED")
                                                .build();
                                orderItemRepository.save(item3);
                        }

                        ShippingInfo ship3 = ShippingInfo.builder()
                                        .order(order3)
                                        .ghnOrderCode("GHN-TEST-003")
                                        .recipientName("Lê Văn C")
                                        .recipientPhone("0912345678")
                                        .recipientAddress("789 Pasteur, Q.3, TP.HCM")
                                        .districtId(1443).wardCode("20201")
                                        .status("delivered")
                                        .build();
                        shippingInfoRepository.save(ship3);

                        // === Order 4: Pending ===
                        Order order4 = Order.builder()
                                        .user(customer)
                                        .orderDate(LocalDateTime.now().minusHours(2))
                                        .subTotal(new BigDecimal("150000"))
                                        .discount(BigDecimal.ZERO)
                                        .totalAmount(new BigDecimal("150000"))
                                        .status(OrderStatus.PENDING)
                                        .build();
                        orderRepository.save(order4);

                        if (variant1 != null) {
                                OrderItem item4 = OrderItem.builder().order(order4).variant(variant1).quantity(1)
                                                .unitPrice(variant1.getPrice()).subTotal(new BigDecimal("150000"))
                                                .discount(BigDecimal.ZERO).totalAmount(new BigDecimal("150000"))
                                                .status("PENDING").build();
                                orderItemRepository.save(item4);
                        }
                        ShippingInfo ship4 = ShippingInfo.builder().order(order4).ghnOrderCode("")
                                        .recipientName("Hoàng Văn D")
                                        .recipientPhone("0988777666")
                                        .recipientAddress("111 Đinh Tiên Hoàng, Q.1, TP.HCM")
                                        .districtId(1442).wardCode("20101").status("ready_to_pick").build();
                        shippingInfoRepository.save(ship4);

                        // === Order 5: Cancelled ===
                        Order order5 = Order.builder()
                                        .user(customer)
                                        .orderDate(LocalDateTime.now().minusDays(1))
                                        .subTotal(new BigDecimal("280000"))
                                        .discount(BigDecimal.ZERO)
                                        .totalAmount(new BigDecimal("280000"))
                                        .status(OrderStatus.CANCELLED)
                                        .build();
                        orderRepository.save(order5);

                        if (variant1 != null) {
                                OrderItem item5 = OrderItem.builder().order(order5).variant(variant1).quantity(2)
                                                .unitPrice(variant1.getPrice()).subTotal(new BigDecimal("280000"))
                                                .discount(BigDecimal.ZERO).totalAmount(new BigDecimal("280000"))
                                                .status("CANCELLED").build();
                                orderItemRepository.save(item5);
                        }
                        ShippingInfo ship5 = ShippingInfo.builder().order(order5).ghnOrderCode("")
                                        .recipientName("Phạm Thị E")
                                        .recipientPhone("0977888999").recipientAddress("222 Lý Tự Trọng, Q.1, TP.HCM")
                                        .districtId(1442).wardCode("20102").status("cancel").build();
                        shippingInfoRepository.save(ship5);

                        // === Order 6: Returned ===
                        Order order6 = Order.builder()
                                        .user(customer)
                                        .orderDate(LocalDateTime.now().minusDays(6))
                                        .subTotal(new BigDecimal("220000"))
                                        .discount(BigDecimal.ZERO)
                                        .totalAmount(new BigDecimal("220000"))
                                        .status(OrderStatus.REFUNDED)
                                        .build();
                        orderRepository.save(order6);

                        if (variant2 != null) {
                                OrderItem item6 = OrderItem.builder().order(order6).variant(variant2).quantity(1)
                                                .unitPrice(variant2.getPrice()).subTotal(new BigDecimal("220000"))
                                                .discount(BigDecimal.ZERO).totalAmount(new BigDecimal("220000"))
                                                .status("RETURNED").build();
                                orderItemRepository.save(item6);
                        }
                        ShippingInfo ship6 = ShippingInfo.builder().order(order6).ghnOrderCode("GHN-RET-001")
                                        .recipientName("Vũ Văn F")
                                        .recipientPhone("0966555444").recipientAddress("333 CMT8, Q.3, TP.HCM")
                                        .districtId(1443).wardCode("20201").status("returned").build();
                        shippingInfoRepository.save(ship6);

                        // === Order 7: Shipping ===
                        Order order7 = Order.builder()
                                        .user(customer)
                                        .orderDate(LocalDateTime.now().minusDays(3))
                                        .subTotal(new BigDecimal("500000"))
                                        .discount(BigDecimal.ZERO)
                                        .totalAmount(new BigDecimal("500000"))
                                        .status(OrderStatus.SHIPPING)
                                        .build();
                        orderRepository.save(order7);

                        if (variant3 != null) {
                                OrderItem item7 = OrderItem.builder().order(order7).variant(variant3).quantity(2)
                                                .unitPrice(variant3.getPrice()).subTotal(new BigDecimal("500000"))
                                                .discount(BigDecimal.ZERO).totalAmount(new BigDecimal("500000"))
                                                .status("SHIPPING").build();
                                orderItemRepository.save(item7);
                        }
                        ShippingInfo ship7 = ShippingInfo.builder().order(order7).ghnOrderCode("GHN-TEST-004")
                                        .recipientName("Đỗ Thị G")
                                        .recipientPhone("0955444333")
                                        .recipientAddress("444 Điện Biên Phủ, Q.Bình Thạnh, TP.HCM")
                                        .districtId(1444).wardCode("20301").status("delivering").build();
                        shippingInfoRepository.save(ship7);

                        articleRepository.saveAll(List.of(article1, article2, article3, article4, article5, article6));
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

                discoveryCombo.getComboItems()
                                .add(ComboItem.builder().combo(discoveryCombo).variant(arabica250).quantity(1).build());
                discoveryCombo.getComboItems()
                                .add(ComboItem.builder().combo(discoveryCombo).variant(robusta500).quantity(1).build());
                discoveryCombo.getComboItems()
                                .add(ComboItem.builder().combo(discoveryCombo).variant(ground200).quantity(1).build());

                comboRepository.save(discoveryCombo);
                log.info("Seeded combo data successfully.");
        }
}
