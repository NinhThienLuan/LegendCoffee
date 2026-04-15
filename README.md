# ☕ Legend Coffee

A full-stack e-commerce web application for a coffee retail business, featuring product management, order processing, online payment integration, promotions, and user authentication.

---

## 👥 Team Members

| Name  | Role     | Responsibilities                                      |
|-------|----------|-------------------------------------------------------|
| Luận  | Backend  | Authentication, Authorization, Security, Data Init    |
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

| Layer      | Technology                          |
|------------|-------------------------------------|
| Backend    | Java / Spring Boot                  |
| Frontend   | Thymleaf                    |
| Database   | MySQL / PostgreSQL                  |
| Payment    | VNPay                               |
| Storage    | Cloudinary (product images)         |
| Auth       | JWT + Spring Security               |

> ⚠️ Update this section to match the actual tech stack used by your team.

---

## 🚀 Getting Started

### Prerequisites

- Java 17+
- Node.js 18+
- MySQL or PostgreSQL
- Cloudinary account
- VNPay sandbox credentials

### Backend Setup

```bash
# Clone the repository
git clone https://github.com/your-org/legend-coffee.git
cd legend-coffee/backend

# Configure environment variables
cp .env.example .env
# Fill in DB credentials, JWT secret, VNPay keys, Cloudinary keys

# Run the application
./mvnw spring-boot:run
```

### Frontend Setup

```bash
cd legend-coffee/frontend

# Install dependencies
npm install

# Configure environment
cp .env.example .env.local
# Fill in API base URL and other config

# Start development server
npm run dev
```

---

## 📁 Project Structure

```
legend-coffee/
├── backend/
│   ├── src/
│   │   ├── auth/          # Luận – Authentication & Security
│   │   ├── order/         # Hào  – Order & Payment (VNPay)
│   │   ├── product/       # Khoa – Product & Cloudinary
│   │   └── promotion/     # Tân  – Promotion & Coupon
│   └── ...
├── frontend/              # Huy & Khánh
│   ├── components/
│   ├── pages/
│   └── ...
└── README.md
```

---

## 🔑 Environment Variables

### Backend (`backend/.env`)

```env
# Database
DB_URL=jdbc:mysql://localhost:3306/legend_coffee
DB_USERNAME=root
DB_PASSWORD=your_password

# JWT
JWT_SECRET=your_jwt_secret
JWT_EXPIRATION=86400000

# VNPay
VNPAY_TMN_CODE=your_tmn_code
VNPAY_HASH_SECRET=your_hash_secret
VNPAY_URL=https://sandbox.vnpayment.vn/paymentv2/vpcpay.html

# Cloudinary
CLOUDINARY_CLOUD_NAME=your_cloud_name
CLOUDINARY_API_KEY=your_api_key
CLOUDINARY_API_SECRET=your_api_secret
```

### Frontend (`frontend/.env.local`)

```env
NEXT_PUBLIC_API_URL=http://localhost:8080/api
```

---

## 📌 API Overview

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

---

## 📄 License

This project is developed for educational purposes as part of a team capstone project.
