package fpt.legendcoffee.common.dataInit;

import fpt.legendcoffee.entity.*;
import fpt.legendcoffee.entity.enumeration.ArticleStatus;
import fpt.legendcoffee.entity.enumeration.OrderStatus;
import fpt.legendcoffee.entity.enumeration.UserRole;
import fpt.legendcoffee.entity.enumeration.WithdrawalStatus;
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
import java.util.Random;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInit implements CommandLineRunner {

        private static final Integer SEED_PROVINCE_ID = 201;
        private static final String SEED_PROVINCE_NAME = "Thành phố Hồ Chí Minh";
        private static final Integer SEED_DISTRICT_ID = 1442;
        private static final String SEED_DISTRICT_NAME = "Quận 1";
        private static final String SEED_WARD_CODE = "20308";
        private static final String SEED_WARD_NAME = "Phường Bến Nghé";
        private static final String EXTRA_ORDER_NOTE_PREFIX = "legendcoffee-seed-extra-orders";
        private static final List<String> SEED_CUSTOMER_EMAILS = List.of(
                        "user@gmail.com",
                        "customer2@legendcoffee.com",
                        "customer3@legendcoffee.com",
                        "customer4@legendcoffee.com",
                        "customer5@legendcoffee.com");

        private final UserRepository userRepository;
        private final ProductRepository productRepository;
        private final OrderRepository orderRepository;
        private final OrderItemRepository orderItemRepository;
        private final ProductVariantRepository productVariantRepository;
        private final ComboRepository comboRepository;
        private final ArticleRepository articleRepository;
        private final CategoryRepository categoryRepository;
        private final ShippingInfoRepository shippingInfoRepository;
        private final WalletRepository walletRepository;
        private final WithdrawalRequestRepository withdrawalRequestRepository;
        private final WalletTransactionRepository walletTransactionRepository;
        private final PasswordEncoder passwordEncoder;

        @Override
        @Transactional
        public void run(@NonNull String... args) {
                log.info("Starting data initialization...");

                seedUsers();
                seedData();
                seedCombos();
                seedOrders();
                seedWalletsAndWithdrawals();

                log.info("Data initialization completed.");
        }

        private void seedUsers() {
                log.info("Seeding users...");

                if (userRepository.findByEmail("admin@legendcoffee.com").isEmpty()) {
                        User admin = User.builder()
                                        .username("Admin Legend")
                                        .email("admin@legendcoffee.com")
                                        .password(passwordEncoder.encode("admin123"))
                                        .phone("0123456789")
                                        .role(UserRole.ADMIN)
                                        .isActive(true)
                                        .build();
                        userRepository.save(admin);
                }

                createCustomerIfMissing("John Doe", "user@gmail.com", "user123", "0333444555");
                createCustomerIfMissing("Jane Smith", "customer2@legendcoffee.com", "user123", "0333444556");
                createCustomerIfMissing("Minh Tran", "customer3@legendcoffee.com", "user123", "0333444557");
                createCustomerIfMissing("Lan Nguyen", "customer4@legendcoffee.com", "user123", "0333444558");
                createCustomerIfMissing("Huy Pham", "customer5@legendcoffee.com", "user123", "0333444559");
        }

        private void createCustomerIfMissing(String username, String email, String rawPassword, String phone) {
                if (userRepository.findByEmail(email).isPresent()) {
                        return;
                }

                User customer = User.builder()
                                .username(username)
                                .email(email)
                                .password(passwordEncoder.encode(rawPassword))
                                .phone(phone)
                                .role(UserRole.USER)
                                .isActive(true)
                                .build();
                userRepository.save(customer);
        }

        private List<User> getSeedCustomers() {
                return SEED_CUSTOMER_EMAILS.stream()
                                .map(email -> userRepository.findByEmail(email).orElse(null))
                                .filter(user -> user != null)
                                .toList();
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
                                        .imageUrl("https://unsplash.com/photos/a-jar-filled-with-coffee-beans-next-to-a-box-jddERMaoNmY")
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
                                        .imageUrl("https://unsplash.com/photos/clear-glass-coffee-pitcher-ZMnBy_vwlCY")
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

                        articleRepository.saveAll(List.of(article1, article2, article3, article4, article5, article6));
                }
        }

        private void seedOrders() {
                List<User> customers = getSeedCustomers();
                List<ProductVariant> variants = productVariantRepository.findAll();

                if (customers.isEmpty() || variants.isEmpty()) {
                        return;
                }

                if (orderRepository.count() == 0) {
                        log.info("Seeding order history from January to now...");

                        Random random = new Random();
                        LocalDateTime now = LocalDateTime.now();

                        // Jan: 6, Feb: 6, Mar: 8, Apr: 7 (Total 27 orders)
                        int[] monthlyCounts = { 6, 6, 8, 7 };
                        int orderSequence = 0;

                        for (int monthIdx = 0; monthIdx < monthlyCounts.length; monthIdx++) {
                                int month = monthIdx + 1;
                                int count = monthlyCounts[monthIdx];

                                for (int i = 0; i < count; i++) {
                                        // Random date in month
                                        int day = random.nextInt(25) + 1;
                                        int hour = random.nextInt(10) + 9; // 9 AM to 7 PM
                                        LocalDateTime orderDate = LocalDateTime.of(2026, month, day, hour,
                                                        random.nextInt(60));

                                        if (orderDate.isAfter(now)) {
                                                orderDate = now.minusMinutes(random.nextInt(120));
                                        }

                                        int userIndex = orderSequence % customers.size();
                                        User customer = customers.get(userIndex);

                                        Order order = Order.builder()
                                                        .user(customer)
                                                        .orderDate(orderDate)
                                                        .discount(BigDecimal.ZERO)
                                                        .status(OrderStatus.COMPLETED)
                                                        .build();

                                        // Variety in status for current month
                                        if (month == 4) {
                                                if (i == 0)
                                                        order.setStatus(OrderStatus.PENDING);
                                                else if (i == 1)
                                                        order.setStatus(OrderStatus.SHIPPING);
                                                else if (i == 2)
                                                        order.setStatus(OrderStatus.CONFIRMED);
                                        } else if (i == count - 1) {
                                                order.setStatus(OrderStatus.CANCELLED);
                                        }

                                        order = orderRepository.save(order);

                                        int numItems = random.nextInt(2) + 1;
                                        BigDecimal subTotal = BigDecimal.ZERO;

                                        for (int j = 0; j < numItems; j++) {
                                                ProductVariant v = variants.get(random.nextInt(variants.size()));
                                                int qty = random.nextInt(2) + 1;
                                                BigDecimal itemTotal = v.getPrice().multiply(BigDecimal.valueOf(qty));

                                                OrderItem item = OrderItem.builder()
                                                                .order(order)
                                                                .variant(v)
                                                                .quantity(qty)
                                                                .unitPrice(v.getPrice())
                                                                .subTotal(itemTotal)
                                                                .discount(BigDecimal.ZERO)
                                                                .totalAmount(itemTotal)
                                                                .status(order.getStatus().name())
                                                                .build();
                                                orderItemRepository.save(item);
                                                subTotal = subTotal.add(itemTotal);
                                        }

                                        order.setSubTotal(subTotal);
                                        order.setTotalAmount(subTotal.add(new BigDecimal("30000"))); // Flat 30k
                                                                                                     // shipping
                                        orderRepository.save(order);

                                        // Shipping Info
                                        String shipStatus = "pending";
                                        switch (order.getStatus()) {
                                                case COMPLETED -> shipStatus = "delivered";
                                                case SHIPPING -> shipStatus = "delivering";
                                                case CANCELLED -> shipStatus = "cancel";
                                                case CONFIRMED -> shipStatus = "ready_to_pick";
                                                case PENDING -> shipStatus = "pending";
                                        }

                                        ShippingInfo ship = ShippingInfo.builder()
                                                        .order(order)
                                                        .recipientName(customer.getUsername())
                                                        .recipientPhone(customer.getPhone())
                                                        .recipientAddress("Số " + (i + 1) + " Đường " + month
                                                                        + ", TP. Hồ Chí Minh")
                                                        .provinceId(SEED_PROVINCE_ID)
                                                        .provinceName(SEED_PROVINCE_NAME)
                                                        .districtId(SEED_DISTRICT_ID)
                                                        .districtName(SEED_DISTRICT_NAME)
                                                        .wardCode(SEED_WARD_CODE)
                                                        .wardName(SEED_WARD_NAME)
                                                        .status(shipStatus)
                                                        .shippingFee(30000L)
                                                        .build();
                                        shippingInfoRepository.save(ship);
                                        orderSequence++;
                                }
                        }
                }

                seedExtraOrders(customers, variants);
        }

        private void seedExtraOrders(List<User> customers, List<ProductVariant> variants) {
                boolean alreadySeeded = shippingInfoRepository.findAll().stream()
                                .anyMatch(info -> info.getNote() != null
                                                && info.getNote().startsWith(EXTRA_ORDER_NOTE_PREFIX));
                if (alreadySeeded) {
                        return;
                }

                log.info("Seeding extra admin orders with target statuses...");

                LocalDateTime now = LocalDateTime.now();
                Random random = new Random();
                List<OrderStatus> targetStatuses = List.of(
                                OrderStatus.PENDING, OrderStatus.PENDING, OrderStatus.PENDING, OrderStatus.PENDING,
                                OrderStatus.PENDING,
                                OrderStatus.CONFIRMED, OrderStatus.CONFIRMED, OrderStatus.CONFIRMED,
                                OrderStatus.CONFIRMED, OrderStatus.CONFIRMED,
                                OrderStatus.SHIPPING, OrderStatus.SHIPPING, OrderStatus.SHIPPING,
                                OrderStatus.SHIPPING, OrderStatus.SHIPPING,
                                OrderStatus.CANCELLED, OrderStatus.CANCELLED, OrderStatus.CANCELLED);

                for (int i = 0; i < targetStatuses.size(); i++) {
                        OrderStatus status = targetStatuses.get(i);
                        LocalDateTime orderDate = now.minusDays(i + 1L);
                        User customer = customers.get(i % customers.size());
                        String shipStatus = switch (status) {
                                case COMPLETED -> "delivered";
                                case SHIPPING -> "delivering";
                                case CANCELLED -> "cancel";
                                case CONFIRMED -> "ready_to_pick";
                                case PENDING -> "pending";
                                default -> "pending";
                        };

                        Order order = Order.builder()
                                        .user(customer)
                                        .orderDate(orderDate)
                                        .discount(BigDecimal.ZERO)
                                        .status(status)
                                        .build();
                        order = orderRepository.save(order);

                        int numItems = random.nextInt(2) + 1;
                        BigDecimal subTotal = BigDecimal.ZERO;

                        for (int j = 0; j < numItems; j++) {
                                ProductVariant v = variants.get(random.nextInt(variants.size()));
                                int qty = random.nextInt(2) + 1;
                                BigDecimal itemTotal = v.getPrice().multiply(BigDecimal.valueOf(qty));

                                OrderItem item = OrderItem.builder()
                                                .order(order)
                                                .variant(v)
                                                .quantity(qty)
                                                .unitPrice(v.getPrice())
                                                .subTotal(itemTotal)
                                                .discount(BigDecimal.ZERO)
                                                .totalAmount(itemTotal)
                                                .status(order.getStatus().name())
                                                .build();
                                orderItemRepository.save(item);
                                subTotal = subTotal.add(itemTotal);
                        }

                        order.setSubTotal(subTotal);
                        order.setTotalAmount(subTotal.add(new BigDecimal("30000")));
                        orderRepository.save(order);

                        ShippingInfo ship = ShippingInfo.builder()
                                        .order(order)
                                        .recipientName(customer.getUsername())
                                        .recipientPhone(customer.getPhone())
                                        .recipientAddress("Seed bổ sung đơn #" + (i + 1))
                                        .provinceId(SEED_PROVINCE_ID)
                                        .provinceName(SEED_PROVINCE_NAME)
                                        .districtId(SEED_DISTRICT_ID)
                                        .districtName(SEED_DISTRICT_NAME)
                                        .wardCode(SEED_WARD_CODE)
                                        .wardName(SEED_WARD_NAME)
                                        .status(shipStatus)
                                        .note(EXTRA_ORDER_NOTE_PREFIX + "-" + (i + 1))
                                        .shippingFee(30000L)
                                        .build();
                        shippingInfoRepository.save(ship);
                }
        }

        private void seedCombos() {
                if (comboRepository.count() > 0 || productVariantRepository.count() == 0) {
                        return;
                }

                log.info("Seeding combos...");
                List<ProductVariant> allVariants = productVariantRepository.findAll();

                ProductVariant arabica250 = findVariant(allVariants, "Legend Arabica Special", "Túi 250g");
                ProductVariant arabica500 = findVariant(allVariants, "Legend Arabica Special", "Túi 500g");
                ProductVariant robusta500 = findVariant(allVariants, "Legend Robusta Bold", "Túi 500g");
                ProductVariant ground200 = findVariant(allVariants, "Espresso Premium Blend (Xay)", "Hộp 200g");
                ProductVariant phin = findVariant(allVariants, "Phin pha cà phê inox", "Size Tiêu Chuẩn");
                ProductVariant v60 = findVariant(allVariants, "Phễu pha V60 Hario", "Size 02");
                ProductVariant grinder = findVariant(allVariants, "Máy xay cà phê cầm tay", "C3");
                ProductVariant paperFilter = findVariant(allVariants, "Giấy lọc V60", "Hộp 100 tờ");

                // 1. Combo Discovery (3 vị) - Trải nghiệm các dòng hạt chủ lực
                if (arabica250 != null && robusta500 != null && ground200 != null) {
                        Combo discoveryCombo = Combo.builder()
                                        .name("Combo Discovery 3 Vị")
                                        .description("Trải nghiệm trọn bộ 3 dòng sản phẩm: Arabica Special, Robusta Bold và Espresso Blend.")
                                        .price(new BigDecimal("499000")) // Tổng lẻ: 150k + 220k + 185k = 555k
                                        .startDate(LocalDateTime.now().minusDays(7))
                                        .endDate(LocalDateTime.now().plusMonths(3))
                                        .isActive(true)
                                        .build();
                        discoveryCombo.getComboItems().add(ComboItem.builder().combo(discoveryCombo).variant(arabica250)
                                        .quantity(2).build());
                        discoveryCombo.getComboItems().add(ComboItem.builder().combo(discoveryCombo).variant(robusta500)
                                        .quantity(2).build());
                        discoveryCombo.getComboItems().add(ComboItem.builder().combo(discoveryCombo).variant(ground200)
                                        .quantity(2).build());
                        comboRepository.save(discoveryCombo);
                }

                // 2. Combo Khởi Đầu (Starter Brew) - Dành cho người mới
                if (phin != null && robusta500 != null) {
                        Combo starterCombo = Combo.builder()
                                        .name("Combo Khởi Đầu")
                                        .description("Bộ đôi hoàn hảo cho người mới: Phin inox cao cấp và cà phê Robusta đậm đà.")
                                        .price(new BigDecimal("265000")) // Tổng lẻ: 85k + 220k = 305k
                                        .startDate(LocalDateTime.now())
                                        .endDate(LocalDateTime.now().plusMonths(6))
                                        .isActive(true)
                                        .build();
                        starterCombo.getComboItems()
                                        .add(ComboItem.builder().combo(starterCombo).variant(phin).quantity(2).build());
                        starterCombo.getComboItems().add(ComboItem.builder().combo(starterCombo).variant(robusta500)
                                        .quantity(2).build());
                        comboRepository.save(starterCombo);
                }

                // 3. Combo Chuyên Nghiệp (Pro Pour-over) - Dành cho tín đồ Pour-over
                if (grinder != null && v60 != null && paperFilter != null && arabica500 != null) {
                        Combo proCombo = Combo.builder()
                                        .name("Combo Chuyên Nghiệp")
                                        .description("Nâng tầm pha chế với máy xay Timemore C3, phễu V60, giấy lọc và Arabica Special.")
                                        .price(new BigDecimal("1850000")) // Tổng lẻ: 1.25M + 450k + 120k + 280k = 2.1M
                                        .startDate(LocalDateTime.now())
                                        .endDate(LocalDateTime.now().plusMonths(6))
                                        .isActive(true)
                                        .build();
                        proCombo.getComboItems()
                                        .add(ComboItem.builder().combo(proCombo).variant(grinder).quantity(2).build());
                        proCombo.getComboItems()
                                        .add(ComboItem.builder().combo(proCombo).variant(v60).quantity(2).build());
                        proCombo.getComboItems().add(
                                        ComboItem.builder().combo(proCombo).variant(paperFilter).quantity(2).build());
                        proCombo.getComboItems().add(
                                        ComboItem.builder().combo(proCombo).variant(arabica500).quantity(2).build());
                        comboRepository.save(proCombo);
                }

                log.info("Seeded combo data successfully.");
        }

        private void seedWalletsAndWithdrawals() {
                if (walletRepository.count() > 0) {
                        return;
                }

                log.info("Seeding wallets and withdrawal requests...");

                List<User> allUsers = userRepository.findAll();
                Random random = new Random();

                for (User user : allUsers) {
                        // 1. Create Wallet
                        BigDecimal available = BigDecimal.valueOf(random.nextInt(5000000) + 1000000); // 1M to 6M
                        BigDecimal reserved = BigDecimal.ZERO;

                        // For some users, make a pending withdrawal to test 'reserved'
                        if (user.getRole() == UserRole.USER && random.nextBoolean()) {
                                reserved = BigDecimal.valueOf(200000);
                                available = available.subtract(reserved);
                        }

                        Wallet wallet = Wallet.builder()
                                        .user(user)
                                        .availableAmount(available)
                                        .reservedAmount(reserved)
                                        .build();
                        walletRepository.save(wallet);

                        // 2. Create some Transactions
                        saveTx(wallet, available.add(reserved), "INCOME", "Số dư khởi tạo hệ thống");

                        // 3. Create Withdrawal Requests for Regular Users
                        if (user.getRole() == UserRole.USER) {
                                // Add one historical approved withdrawal
                                WithdrawalRequest approved = WithdrawalRequest.builder()
                                                .user(user)
                                                .amount(BigDecimal.valueOf(150000))
                                                .bankName("Vietcombank")
                                                .bankCode("VCB")
                                                .accountNumber("001100" + random.nextInt(1000000))
                                                .accountHolder(user.getUsername().toUpperCase())
                                                .status(WithdrawalStatus.APPROVED)
                                                .processedAt(LocalDateTime.now().minusDays(5))
                                                .build();
                                withdrawalRequestRepository.save(approved);
                                saveTx(wallet, approved.getAmount(), "EXPENSE", "Đã rút tiền về ngân hàng VCB");

                                // Add one rejected withdrawal
                                WithdrawalRequest rejected = WithdrawalRequest.builder()
                                                .user(user)
                                                .amount(BigDecimal.valueOf(50000))
                                                .bankName("Techcombank")
                                                .bankCode("TCB")
                                                .accountNumber("1903" + random.nextInt(1000000))
                                                .accountHolder(user.getUsername().toUpperCase())
                                                .status(WithdrawalStatus.REJECTED)
                                                .rejectReason("Thông tin tài khoản không trùng khớp")
                                                .processedAt(LocalDateTime.now().minusDays(2))
                                                .build();
                                withdrawalRequestRepository.save(rejected);

                                // Add one pending withdrawal if we set reserved earlier
                                if (reserved.compareTo(BigDecimal.ZERO) > 0) {
                                        WithdrawalRequest pending = WithdrawalRequest.builder()
                                                        .user(user)
                                                        .amount(reserved)
                                                        .bankName("MB Bank")
                                                        .bankCode("MB")
                                                        .accountNumber("9999" + random.nextInt(1000000))
                                                        .accountHolder(user.getUsername().toUpperCase())
                                                        .status(WithdrawalStatus.PENDING)
                                                        .build();
                                        withdrawalRequestRepository.save(pending);
                                }
                        }
                }
        }

        private void saveTx(Wallet wallet, BigDecimal amount, String type, String desc) {
                WalletTransaction tx = WalletTransaction.builder()
                                .wallet(wallet)
                                .amount(amount)
                                .transactionType(type)
                                .description(desc)
                                .build();
                walletTransactionRepository.save(tx);
        }

        private ProductVariant findVariant(List<ProductVariant> variants, String productName, String variantName) {
                return variants.stream()
                                .filter(v -> v.getProduct() != null
                                                && productName.equals(v.getProduct().getName())
                                                && variantName.equals(v.getVariantName()))
                                .findFirst()
                                .orElse(null);
        }
}
