# AI History - Tan

## Session: 18-04-2026
- Date: 2026-04-18

### User requests and issues
- Khong ket noi duoc database.
- Loi JPA: `Voucher.type` dung `@Enumerated` voi kieu `String`.
- Loi compile: `VoucherType` khong ep ve `String`.
- Loi Spring MVC ambiguous mapping trong `VoucherController` (`PUT /voucher/{id}`).
- Tao nhanh, push commit, kiem tra merge request.
- Loi Spring MVC ambiguous mapping giua `ProductController#listProducts` va `CatalogController#adminProducts` cho `GET /products`.

### Assistant notes
- Nguyen nhan chinh ban dau khong phai ket noi database, ma la loi mapping/entity va route conflict lam app fail truoc khi vao runtime binh thuong.
- Can tach ro endpoint bi trung nhau (VD: doi duong dan update/unActive voucher, va doi 1 trong 2 route `/products`).

### Next update format
- Date:
- Time:
- User:
- Assistant:
- Files touched:
- Result:
