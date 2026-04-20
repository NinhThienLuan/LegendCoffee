# AI History - Tan

## Session: 18-04-2026
- Date: 2026-04-18

### User requests and issues
- Muốn xây dựng hệ thống discount cho web bán cafe.
- Tìm hiểu sự khác nhau giữa promotion và coupon/voucher.
- Yêu cầu thiết kế promotion dạng chung nhưng tách thành `VariantPromotion` và `Voucher`.
- Cần xác định quan hệ giữa `Promotion`, `VariantPromotion`, `ProductVariant`.
- Hỏi cách tổ chức service cho `VariantPromotion`.
- Xác nhận việc sử dụng DTO khi trả về list dữ liệu.
- Yêu cầu thiết kế các DTO:
  - `ProductVariantDTO`
  - `PromotionDTO`
  - `VariantPromotionDTO`
- Xây dựng API controller cho `VariantPromotion` và kiểm tra tính đầy đủ.
- Hỏi về việc có nên update/delete promotion.
- Yêu cầu viết tài liệu tổng hợp (chat history).
- Yêu cầu định dạng lại tài liệu theo form chuẩn.

### Assistant notes
- Hệ thống discount được tách thành 2 phần rõ ràng:
  - `Voucher`: áp dụng cho đơn hàng, có thể nhập code.
  - `Promotion`: áp dụng tự động theo sản phẩm.
- Thiết kế dữ liệu:
  - `Promotion`: chứa thông tin giảm giá.
  - `VariantPromotion`: bảng liên kết giữa promotion và product variant.
  - `Voucher`: độc lập, áp dụng cho order.
- Quan hệ chính:
  - `Promotion` (1) → `VariantPromotion` (N) → `ProductVariant`.
- DTO được sử dụng để:
  - Tránh trả entity trực tiếp.
  - Tránh lỗi lazy loading.
  - Giảm dữ liệu dư thừa.
- Các chức năng chính của `VariantPromotion`:
  - Gán promotion cho variant.
  - Bỏ promotion khỏi variant.
  - Lấy danh sách promotion theo variant.
  - Lấy danh sách variant theo promotion.
- Nguyên tắc xử lý promotion:
  - Có thời gian hiệu lực.
  - Có thể bật/tắt bằng trạng thái.
  - Không nên xóa cứng.
- Luồng áp dụng giảm giá:
  - Order → áp dụng promotion (tự động) → áp dụng voucher (nếu có) → tính tổng tiền.

### Additional session updates (20-04-2026)
- User reports không kết nối được database.
- Assistant identifies nguyên nhân chính là lỗi startup:
  - JPA mapping lỗi ở `Voucher.type` do `@Enumerated` không khớp kiểu `String`.
  - Compile lỗi `VoucherType` không convert sang `String`.
  - Ambiguous mapping ở `VoucherController` (`PUT /voucher/{id}`).
  - Ambiguous mapping ở `GET /products` giữa `ProductController` và `CatalogController`.
- User asks thao tác Git:
  - Tạo branch `feat/promotion`.
  - Push commit đã có, không push phần chưa commit.
  - Push toàn bộ commit đã local.
  - Kiểm tra trạng thái merge request.
- Assistant verifies và push thành công 2 commit lên `origin/feat/promotion`:
  - `82192a7` - `feat: AI chat history`
  - `b280506` - `fix: Complete the variable promotion feature`
- User asks tạo nơi lưu lịch sử AI riêng.
- Assistant tạo `ai_history/Tan/chat-history.md` và bổ sung trường `Date` trong template.
- User asks chuẩn hóa lại file history theo form cũ.
- Assistant reformat file hiện tại về đúng cấu trúc chuẩn và đồng bộ `Session`/`Date`.

### Next update format
- Date:
- Time:
- User:
- Assistant:
- Files touched:
- Result:
