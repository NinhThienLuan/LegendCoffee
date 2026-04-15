# ☕ Legend Coffee

A full-stack e-commerce web application for a coffee retail business, featuring product management, order processing, online payment integration, promotions, and user authentication.

---

## 👥 Team Members

| Name  | Role     | Responsibilities                                      |
|-------|----------|-------------------------------------------------------|
| Luân  | Backend  | Authentication, Authorization, Security, Data Init    |
| Hào   | Backend  | Payment, Order Management, VNPay Integration          |
| Khoa  | Backend  | Product Management, Cloudinary Integration            |
| Tân   | Backend  | Promotion & Coupon System                             |
| Huy   | Frontend | UI/UX Development                                     |
| Khánh | Frontend | UI/UX Development                                     |

---

## 🧩 Features

- 🔐 **Authentication & Authorization** — JWT-based login/register, role-based access control (Admin/User)
- 🛒 **Order Management** — Place, confirm, and track orders with itemized breakdowns
- 💳 **Payment** — VNPay payment gateway integration with transaction tracking
- 📦 **Product Management** — Categories, products, variants (Pack/KG), stock, Cloudinary image hosting
- 🎁 **Promotions & Coupons** — Discount campaigns on product variants, voucher codes (% or fixed)
- 🖥️ **Frontend** — Responsive UI for browsing products, cart, checkout, and order history

---

## 🗄️ Database Schema

```
ROLE         → USER → ORDER → PAYMENT
                             ↓
                         ORDER_ITEM ← PRODUCT_VARIANT ← PRODUCT ← CATEGORY
                                              ↓
                                      VARIANT_PROMOTION ← PROMOTION
ORDER → VOUCHER
```

### Main Entities

| Entity            | Description                                                   |
|-------------------|---------------------------------------------------------------|
| `ROLE`            | User roles (e.g., Admin, Customer)                            |
| `USER`            | Customer accounts with contact & address info                 |
| `ORDER`           | Purchase orders with status, subtotal, discount, total        |
| `ORDER_ITEM`      | Line items per order linked to a product variant              |
| `PAYMENT`         | Payment records with VNPay transaction refs & error handling  |
| `PRODUCT`         | Coffee products with origin, description, expiry info         |
| `CATEGORY`        | Product categories                                            |
| `PRODUCT_VARIANT` | SKU-level variants (size, packaging, price, stock)            |
| `PROMOTION`       | Time-based discount campaigns on variants                     |
| `VARIANT_PROMOTION` | Many-to-many join between promotions and variants           |
| `VOUCHER`         | Order-level voucher codes (% or fixed discount)               |

---

## 🛠️ Tech Stack

| Layer       | Technology                              |
|-------------|------------------------------------------|
| **Runtime** | Java 21 (JDK 21+)                      |
| **Framework** | Spring Boot 4.0.5                     |
| **View Engine** | Thymeleaf 3.x                        |
| **Database** | H2 (development) / SQL Server (production) |
| **ORM**     | Spring Data JPA                         |
| **Build Tool** | Maven 3.6+                           |
| **Utilities** | Lombok                                 |
| **Security** | JWT + Spring Security                  |
| **Payment** | VNPay Gateway                          |
| **Storage** | Cloudinary (product images)            |

**Note:** Spring Boot 4.0.5 requires Java 21+. Thymeleaf templates are rendered server-side for dynamic content.


---

## 🚀 Getting Started

### Prerequisites

- Java 21+
- Maven 3.6+
- SQL Server or H2 Database
- Cloudinary account
- VNPay sandbox credentials

### Backend Setup

```bash
# Clone the repository
git clone https://github.com/your-org/legend-coffee.git
cd legend-coffee

# Configure environment variables
cp .env.example .env
# Fill in DB credentials, JWT secret, VNPay keys, Cloudinary keys

# Run the application
./mvnw spring-boot:run
```

The application will start at `http://localhost:8080`

---

## 📁 Project Structure

```
legend-coffee/
├── src/
│   ├── main/
│   │   ├── java/fpt/legendcoffee/
│   │   │   ├── auth/          # Luân – Authentication & Security
│   │   │   ├── order/         # Hào  – Order & Payment (VNPay)
│   │   │   ├── product/       # Khoa – Product & Cloudinary
│   │   │   ├── promotion/     # Tân  – Promotion & Coupon
│   │   │   ├── controller/    # MVC Controllers (REST & Thymeleaf)
│   │   │   ├── entity/        # JPA Entities
│   │   │   ├── repository/    # Spring Data JPA Repositories
│   │   │   ├── service/       # Business Logic
│   │   │   └── LegendcoffeeApplication.java
│   │   └── resources/
│   │       ├── templates/
│   │       │   ├── authen/
│   │       │   │   ├── login/
│   │       │   │   │   └── login.html
│   │       │   │   └── register/
│   │       │   │       └── register.html
│   │       │   ├── fragments/
│   │       │   │   ├── navbar.html
│   │       │   │   ├── footer.html
│   │       │   │   ├── head.html
│   │       │   │   └── catalogs.html
│   │       │   ├── catalog-detail.html
│   │       │   ├── catalogs.html
│   │       │   └── index.html
│   │       ├── static/
│   │       │   └── assets/
│   │       │       ├── app.css
│   │       │       └── effects.js
│   │       └── application.properties
│   └── test/
│       └── java/fpt/legendcoffee/
├── pom.xml
├── mvnw
├── mvnw.cmd
└── README.md
```

**Thymeleaf Template Path Convention:**
- Templates in `src/main/resources/templates/` are resolved as view names
- When a controller returns `"authen/login/login"`, Spring Boot looks for `src/main/resources/templates/authen/login/login.html`
- Fragments (reusable components) are in `src/main/resources/templates/fragments/`

---

## 🔑 Environment Variables

### Backend (`application.properties`)

```properties
# Application
spring.application.name=legendcoffee
server.port=8080

# Database
spring.datasource.url=jdbc:h2:mem:testdb
spring.datasource.driverClassName=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=

# H2 Console
spring.h2.console.enabled=true
spring.h2.console.path=/h2-console

# JPA
spring.jpa.database-platform=org.hibernate.dialect.H2Dialect
spring.jpa.hibernate.ddl-auto=update

# JWT
jwt.secret=your_jwt_secret_key_here
jwt.expiration=86400000

# VNPay
vnpay.tmnCode=your_tmn_code
vnpay.hashSecret=your_hash_secret
vnpay.url=https://sandbox.vnpayment.vn/paymentv2/vpcpay.html

# Cloudinary
cloudinary.cloudName=your_cloud_name
cloudinary.apiKey=your_api_key
cloudinary.apiSecret=your_api_secret
```

---

## 📌 API Overview

### REST Endpoints

| Module       | Base Path           | Owner |
|--------------|---------------------|-------|
| Auth         | `/api/auth`         | Luân  |
| Users        | `/api/users`        | Luân  |
| Products     | `/api/products`     | Khoa  |
| Categories   | `/api/categories`   | Khoa  |
| Orders       | `/api/orders`       | Hào   |
| Payments     | `/api/payments`     | Hào   |
| Promotions   | `/api/promotions`   | Tân   |
| Vouchers     | `/api/vouchers`     | Tân   |

### Thymeleaf Views

Thymeleaf templates are located in `src/main/resources/templates/` and handle server-side rendering for web pages.

---

## 📄 License

This project is developed for educational purposes as part of a team capstone project.
