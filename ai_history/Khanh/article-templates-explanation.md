# Giải thích Chi tiết Toàn bộ Mã nguồn Template - Article Management

Tài liệu này phân tích từng dòng mã trong 3 tệp template chính để bạn nắm rõ cấu tạo của chúng.

---

## 1. File `admin/article-form.html` (Form Thêm/Sửa Bài Viết)

Tệp này sử dụng Grid layout và các thẻ Thymeleaf để tạo form nhập liệu.

### Cấu trúc cơ bản (Dòng 1 - 11):
- **Dòng 1-2:** Khai báo HTML5 và namespace Thymeleaf (`xmlns:th`). Class `light` phục vụ cho theme sáng.
- **Dòng 4:** `<head th:replace="...">`: Nhúng phần `<head>` dùng chung (CSS, Google Fonts, Meta tags) từ file `fragments/head.html`.
- **Dòng 6:** Thẻ `body` với các class Tailwind: `bg-surface` (màu nền), `text-on-surface` (màu chữ), `selection:bg-primary-container` (màu khi bôi đen văn bản).
- **Dòng 8:** Nhúng Sidebar quản trị bên trái. `ml-64` ở dòng 9 tạo khoảng trống cho Sidebar này.
- **Dòng 10:** Nhúng Topbar (thanh tiêu đề phía trên) của trang quản trị.

### Form nhập liệu (Dòng 13 - 68):
- **Dòng 13:** `<form ... th:object="${article}">`: Đối tượng `${article}` được Controller truyền qua `model.addAttribute("article", ...)`.
- **Dòng 16:** `<input type="hidden" th:field="*{id}" />`: Lưu ID của bài viết. Nếu là tạo mới, ID này trống. Nếu là sửa, nó chứa ID hiện tại.
- **Dòng 17:** `<div class="grid md:grid-cols-2 gap-4">`: Chia bố cục thành 2 cột trên màn hình máy tính (md).
- **Dòng 20-22 (Tiêu đề):** Ô nhập văn bản liên kết với trường `title`. `border-outline-variant` là màu viền theo Design System.
- **Dòng 31-33 (Tóm tắt):** Ô `textarea` cho trường `summary`. `rows="2"` giới hạn độ cao ban đầu.
- **Dòng 37-38 (Ảnh bìa):** Nhập URL ảnh. Thuộc tính `type="url"` giúp trình duyệt kiểm tra định dạng web link.
- **Dòng 42-46 (Trạng thái):** Thẻ `select` cho phép chọn `DRAFT`, `PUBLISHED`, hoặc `ARCHIVED`. Thymeleaf sẽ tự động chọn đúng giá trị dựa trên dữ liệu hiện có trong đối tượng.
- **Dòng 51-53 (Nội dung JSON):** Ô nhập liệu lớn cho trường `contentJson`. Class `font-mono` giúp code/JSON dễ đọc hơn.
- **Dòng 56 (Nút Lưu):** `type="submit"` gửi toàn bộ dữ liệu form về URL `@{/admin/articles/save}`.
- **Dòng 60-62 (Nút Xóa):** Chỉ hiển thị (`th:if`) nếu bài viết đã có ID.
- **Dòng 63-65 (Nút Xem):** Link dẫn đến trang hiển thị chi tiết bài viết phía người dùng.

---

## 2. File `admin/posts.html` (Danh Sách Bài Viết Quản Trị)

Tệp này có cấu trúc phức tạp hơn với các thống kê và bảng dữ liệu.

### Thống kê (Dòng 20 - 66):
- **Dòng 33:** `th:text="${#lists.size(articles)}"`: Sử dụng hàm `#lists.size` của Thymeleaf để đếm số lượng phần tử trong danh sách gửi từ Controller.
- **Dòng 54-64:** Hero section với ảnh nền từ Unsplash và hiệu ứng gradient overlay màu xanh (`bg-primary/90`).

### Bảng danh sách (Dòng 69 - 165):
- **Dòng 76-80:** Nút "Thêm bài viết" dẫn đến link `/admin/articles/new`.
- **Dòng 89-95:** Tiêu đề bảng (`<thead>`) với chiều rộng các cột được quy định bằng `%` (ví dụ `w-[28%]`).
- **Dòng 100:** `th:each="art : ${articles}"`: Vòng lặp duyệt danh sách.
- **Dòng 102 (Tiêu đề):** `text-primary text-sm truncate`: Chữ màu xanh, kích thước nhỏ và tự động cắt ngắn (`truncate`) nếu tiêu đề quá dài.
- **Dòng 107-108 (Tác giả):** Kiểm tra null để tránh lỗi nếu bài viết không gắn với User nào.
- **Dòng 115:** `#temporals.format(...)`: Định dạng thời gian `LocalDateTime` sang chuỗi `dd/MM/yyyy`.
- **Dòng 119-122 (Trạng thái):**
    - `th:text="${art.status}"`: Hiển thị chữ trạng thái.
    - `th:classappend`: Thêm class màu sắc linh hoạt (xanh cho Đã đăng, xám cho Bản nháp, vàng cho Lưu trữ).
- **Dòng 125-136 (Hành động):** Chứa nút Sửa (link) và nút Xóa (form submit).

### Pagination & FAB (Dòng 143 - 226):
- **Dòng 143-163:** Phân trang mẫu (hiện tại đang là giao diện tĩnh).
- **Dòng 223-226:** FAB (Floating Action Button) - Nút tròn màu xanh nằm cố định ở góc dưới bên phải màn hình để tạo bài viết nhanh.

---

## 3. File `articles/articles.html` (Chi Tiết Bài Viết)

Tệp này xử lý hiển thị nội dung chuyên sâu cho người đọc cuối (không phải admin). Giao diện bao gồm Hero ảnh bìa toàn màn hình, layout 3 cột (sidebar trái – nội dung chính – sidebar phải), và hệ thống render JSON động.

### Cấu trúc cơ bản (Dòng 1 - 26):
- **Dòng 3:** `<html class="light">` — áp dụng theme sáng, không có `xmlns:th` vì tệp dùng Thymeleaf inline thay vì attribute-based hoàn toàn.
- **Dòng 5:** Nhúng `<head>` dùng chung qua fragment, truyền title trang cụ thể.
- **Dòng 7:** `font-body` áp dụng font chữ mặc định toàn trang. `selection:bg-primary-container` tô màu khi người dùng bôi đen văn bản theo đúng Design System.
- **Dòng 8-22 (`<style>` nội tuyến):** Định nghĩa 2 class CSS riêng cho nội dung bài viết (`article-content`), vì Tailwind không thể áp dụng trực tiếp cho HTML được sinh ra động bởi JavaScript. `p` được căn chỉnh `line-height: 1.8` cho dễ đọc, `h2` dùng font Manrope đậm cỡ 1.5rem để nổi bật tiêu đề phần.
- **Dòng 25:** Nhúng Navbar điều hướng chính. `${navItems}` là danh sách menu được Controller truyền vào.
- **Dòng 26:** `pt-16` tạo khoảng cách đẩy nội dung xuống dưới Navbar cố định.

### Hero Header (Dòng 28 - 60):
- **Dòng 28:** Container `header` cao `716px` (`h-[716px]`) với `flex items-end pb-16` — đẩy toàn bộ nội dung chữ xuống phía dưới ảnh, tạo cảm giác ảnh chiếm ưu thế.
- **Dòng 29-33 (Ảnh bìa):** `th:src` kiểm tra 3 điều kiện trước khi lấy URL ảnh: bài viết phải tồn tại (`article != null`), trường ảnh phải có giá trị (`article.coverImageUrl != null`), và chuỗi không được rỗng (`!#strings.isEmpty(...)`). Nếu bất kỳ điều kiện nào sai, fallback sang ảnh mặc định Unsplash. `opacity-60 mix-blend-multiply` làm ảnh tối xuống tự nhiên, giúp chữ trắng bên trên dễ đọc.
- **Dòng 35-42 (Tags danh mục):** 2 badge nhỏ viền bo tròn ở trên tiêu đề. Hiện đang là nội dung tĩnh, chưa được bind động từ dữ liệu bài viết.
- **Dòng 43-47 (Tiêu đề H1):** `th:text` kiểm tra null trước khi hiển thị `article.title`. Nếu không có bài viết, fallback sang tiêu đề demo. `text-5xl md:text-7xl` — chữ nhỏ hơn trên điện thoại, rất to trên màn hình rộng.
- **Dòng 48-58 (Meta thông tin):** Hiển thị ngày bằng `#temporals.format(article.createdAt, 'dd/MM/yyyy')` — hàm tiện ích Thymeleaf để định dạng `LocalDateTime`. Thời gian đọc (`12 phút đọc`) hiện là tĩnh, chưa tính động từ độ dài nội dung.

### Layout 3 cột (Dòng 62 - 248):
- **Dòng 62:** `grid grid-cols-1 lg:grid-cols-12 gap-16` — trên mobile hiển thị 1 cột, trên màn hình lớn chia 12 cột với khoảng cách `64px` giữa các vùng.

#### Sidebar trái - Tương tác (Dòng 64 - 88):
- **Dòng 64:** `hidden lg:block lg:col-span-1` — ẩn hoàn toàn trên mobile, chỉ hiện trên màn hình lớn và chiếm 1/12 chiều rộng. `border-r border-outline-variant/30` tạo đường kẻ mờ ngăn cách với nội dung chính.
- **Dòng 65:** `sticky top-28` — sidebar dính theo khi cuộn trang, bắt đầu từ vị trí `112px` từ đỉnh (tránh bị che bởi Navbar).
- **Dòng 66-86 (3 nút tương tác):** Mỗi nút gồm một vòng tròn `w-12 h-12` chứa icon Material Symbol và số đếm bên dưới. `hover:bg-primary hover:text-white transition-all duration-300` tạo hiệu ứng đổi màu mượt khi hover. Ba nút lần lượt là: Yêu thích (1.2k), Bình luận (48), Chia sẻ.

#### Nội dung chính (Dòng 90 - 186):
- **Dòng 90:** `lg:col-span-7 article-content` — chiếm 7/12 chiều rộng và áp dụng CSS tùy chỉnh cho kiểu chữ bài viết.
- **Dòng 91-93 (Tóm tắt):** `th:if` kiểm tra null trước khi render. `border-l-4 border-primary pl-6` tạo kiểu blockquote với thanh viền xanh bên trái — nổi bật phần tóm tắt. `th:text="${article.summary}"` bind trực tiếp nội dung tóm tắt.
- **Dòng 95-96 (Vùng render JSON):** `div#article-content-render` là vùng trống, JavaScript sẽ điền HTML vào đây sau khi xử lý xong `contentJson`. `th:if` đảm bảo cả div lẫn `<script>` chứa JSON chỉ xuất hiện khi bài viết có nội dung thực sự.
- **Dòng 97-100 (Truyền JSON sang JS):** `<script th:inline="javascript">` cho phép Thymeleaf nhúng biến Java vào JavaScript. Cú pháp `/*[[${article.contentJson}]]*/` là cách Thymeleaf xuất giá trị an toàn vào ngữ cảnh JavaScript — dấu comment `/* */` giúp IDE không báo lỗi cú pháp trong khi Thymeleaf vẫn xử lý được.
- **Dòng 102-136 (Nội dung fallback):** `th:if` với điều kiện ngược — chỉ hiển thị khi bài viết không có `contentJson`. Đây là bài viết mẫu tĩnh dùng để demo giao diện khi chưa có dữ liệu thật.
- **Dòng 137-151 (Thẻ bài viết):** Danh sách tags dạng pill tròn (`rounded-full`) với `hover:bg-surface-container-high`. Hiện tĩnh, chưa bind từ entity.
- **Dòng 154-185 (Bình luận):** Phần bình luận với form gửi và một comment mẫu hiển thị cứng. Chưa tích hợp backend xử lý gửi bình luận thật.

#### Sidebar phải - Nội dung liên quan (Dòng 188 - 247):
- **Dòng 188:** `lg:col-span-4` — chiếm 4/12 chiều rộng, phần còn lại sau sidebar trái (1) và nội dung (7).
- **Dòng 189-216 (Bài viết liên quan):** Danh sách 3 bài viết liên quan dạng link, mỗi bài có nhãn danh mục (`text-xs uppercase tracking-widest`) và tiêu đề với `group-hover:text-primary` — toàn bộ vùng `<a>` khi hover thì tiêu đề đổi màu xanh. Hiện là nội dung tĩnh.
- **Dòng 218-235 (CTA Banner):** Khối ảnh nền với overlay tối (`brightness-50`) và nút "Yêu cầu báo giá". `group-hover:scale-105 transition-transform duration-700` — ảnh phóng to nhẹ khi hover toàn khối, tạo hiệu ứng sống động.
- **Dòng 236-246 (Đăng ký bản tin):** Form đơn giản gồm `input[type=email]` và nút "Đăng ký". `border-l-2 border-surface-container-high` tạo đường viền trái mỏng phân biệt khối này với phần trên.

### JavaScript xử lý nội dung (Dòng 255 - 466):
Toàn bộ khối JS được bọc trong IIFE (`(function() { ... })()`), ngăn biến rò rỉ ra phạm vi toàn cục.

- **Dòng 254:** Tải thư viện DOMPurify từ CDN jsDelivr — thư viện bảo mật giúp lọc XSS trong HTML được sinh ra động.
- **Dòng 257-260 (Khởi động):** Tìm phần tử `#article-content-render`. Nếu không tồn tại (trang không có bài viết), dừng ngay lập tức bằng `return`.

#### Hàm `escapeHtml` (Dòng 262 - 269):
Chuyển đổi 5 ký tự đặc biệt HTML thành thực thể an toàn: `&` → `&amp;`, `<` → `&lt;`, `>` → `&gt;`, `"` → `&quot;`, `'` → `&#039;`. Mục đích: khi hiển thị nội dung văn bản thuần túy bên trong thẻ HTML, ngăn trình duyệt hiểu nhầm là mã HTML thật.

#### Hàm `sanitizeImageUrl` (Dòng 271 - 300):
Kiểm tra URL ảnh trước khi nhúng vào thẻ `<img>`. Từ chối ngay các URL bắt đầu bằng `javascript:`, `data:`, `vbscript:` — đây là các vector tấn công XSS phổ biến qua thuộc tính `src`. Dùng `new URL()` để parse và chỉ chấp nhận protocol `http:` hoặc `https:`.

#### Hàm `sanitizePlainText` (Dòng 302 - 320):
Làm sạch văn bản thô qua 6 bước tuần tự: dùng `textarea` DOM để decode HTML entities, xóa toàn bộ thẻ HTML bằng regex `/<[^>]*>/g`, loại bỏ `javascript:`, `vbscript:`, các sự kiện inline `on*=`, từ khóa `script`, cuối cùng dùng Unicode property escapes `\p{L}\p{N}` để chỉ giữ lại chữ cái và số hợp lệ.

#### Hàm `sanitizeIncomingData` (Dòng 322 - 377):
Duyệt qua mảng `blocks` từ JSON Editor.js. Mỗi block phải có đủ `type` và `data`, các block không hợp lệ bị bỏ qua. Với mỗi loại block được hỗ trợ (`header`, `paragraph`, `list`, `quote`, `image`), hàm tạo một object mới sạch (`nextBlock`) thay vì dùng lại object gốc — tránh các trường ẩn không mong muốn. Block không thuộc các loại đã biết bị loại bỏ hoàn toàn.

#### Hàm `renderBlocksToHtml` (Dòng 379 - 435):
Chuyển đổi mảng block đã được làm sạch thành chuỗi HTML:
- `header` → `<h1>` đến `<h6>` tùy `level`, mặc định là `<h2>` nếu level không hợp lệ.
- `paragraph` → `<p>` bọc văn bản đã escape.
- `list` → `<ul>` hoặc `<ol>` với các `<li>` bên trong, tùy `style` là `unordered` hay `ordered`.
- `quote` → `<blockquote>` gồm `<p>` cho nội dung và `<cite>` cho tên tác giả.
- `image` → `<figure>` bọc `<img loading="lazy">` và `<figcaption>` nếu có caption. `loading="lazy"` giúp ảnh chỉ tải khi cuộn đến, cải thiện hiệu năng trang.

#### Luồng thực thi chính (Dòng 437 - 466):
1. Đọc `window.__ARTICLE_CONTENT_JSON` — biến được Thymeleaf nhúng vào ở dòng 99.
2. `JSON.parse()` chuỗi JSON thô, kiểm tra `parsed.blocks` phải là mảng.
3. Gọi `sanitizeIncomingData()` để lọc toàn bộ nội dung.
4. Gọi `renderBlocksToHtml()` để sinh HTML.
5. Nếu DOMPurify có mặt, chạy thêm một lớp lọc cuối với danh sách `ALLOWED_TAGS` và `ALLOWED_ATTR` được kiểm soát chặt — chỉ cho phép các thẻ nội dung an toàn, cấm hoàn toàn `data-*` attributes.
6. Gán `innerHTML` cho `#article-content-render`. Nếu có lỗi parse, `console.error()` ghi log nhưng không crash trang.

---

## Tổng kết kiến trúc 3 tầng bảo mật

Hệ thống render nội dung được bảo vệ bởi 3 lớp độc lập:

1. **Tầng 1 - Thymeleaf (Server):** `th:text`, `th:if`, `th:src` tự động escape output, ngăn XSS ngay từ lúc render HTML phía server.
2. **Tầng 2 - JavaScript tự viết:** `escapeHtml`, `sanitizePlainText`, `sanitizeImageUrl`, `sanitizeIncomingData` lọc toàn bộ dữ liệu JSON trước khi sinh HTML.
3. **Tầng 3 - DOMPurify (Thư viện):** Lớp kiểm tra cuối cùng bằng allowlist, loại bỏ bất kỳ thẻ hoặc thuộc tính nguy hiểm nào còn sót lại.

Mã nguồn này được viết theo chuẩn hiện đại, sử dụng Tailwind CSS cho giao diện, Thymeleaf cho logic hiển thị dữ liệu từ Spring Boot, và JavaScript thuần (không dùng framework) cho phần xử lý nội dung động phía client.