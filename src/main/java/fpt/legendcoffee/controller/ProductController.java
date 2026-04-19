package fpt.legendcoffee.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import fpt.legendcoffee.dto.request.ProductRequestDTO;
import fpt.legendcoffee.service.ProductService;
import lombok.RequiredArgsConstructor;
@Controller
@RequestMapping("/products")
@RequiredArgsConstructor
public class ProductController {

    private static final Logger log = LoggerFactory.getLogger(ProductController.class);

    private final ProductService productService;

    @GetMapping("/list")
    public String getAllProducts(Model model) {
        model.addAttribute("products", productService.getAllProducts());
        return "admin/products";
    }

    @GetMapping("/form-add")
    public String addProductForm(Model model) {
        Object requestAttr = model.getAttribute("request");
        if (!(requestAttr instanceof ProductRequestDTO)) {
            model.addAttribute("request", new ProductRequestDTO());
        }
        model.addAttribute("isEdit", false);
        return "product/add-product";
    }

    @PostMapping("/add")
    public String addProduct(@ModelAttribute("request") ProductRequestDTO request,
            RedirectAttributes redirectAttributes) {
        try {
            productService.addProduct(request);
            log.info("[Product] Thêm sản phẩm thành công - name={}", request.getName());
            redirectAttributes.addFlashAttribute("success", "Thêm sản phẩm thành công.");
            return "redirect:/products/list";
        } catch (IllegalArgumentException e) {
            log.warn("[Product] Lỗi validate khi thêm sản phẩm - {}", e.getMessage());
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            redirectAttributes.addFlashAttribute("request", sanitizeOrEmpty(request));
            return "redirect:/products/form-add";
        } catch (MaxUploadSizeExceededException e) {
            log.warn("[Product] File ảnh vượt quá dung lượng cho phép", e);
            redirectAttributes.addFlashAttribute("error", "Ảnh tải lên quá lớn. Vui lòng chọn file nhỏ hơn.");
            redirectAttributes.addFlashAttribute("request", sanitizeOrEmpty(request));
            return "redirect:/products/form-add";
        } catch (DataIntegrityViolationException e) {
            String errorCode = nextErrorCode();
            log.error("[Product][{}] Lỗi ràng buộc dữ liệu khi thêm sản phẩm | rootCause={}",
                    errorCode, getRootCauseMessage(e), e);
            redirectAttributes.addFlashAttribute("error", "Dữ liệu không hợp lệ hoặc bị trùng. Mã lỗi: " + errorCode);
            redirectAttributes.addFlashAttribute("request", sanitizeOrEmpty(request));
            return "redirect:/products/form-add";
        } catch (DataAccessException e) {
            String errorCode = nextErrorCode();
            log.error("[Product][{}] Lỗi truy cập dữ liệu khi thêm sản phẩm | rootCause={}",
                    errorCode, getRootCauseMessage(e), e);
            redirectAttributes.addFlashAttribute("error", "Lỗi cơ sở dữ liệu. Mã lỗi: " + errorCode);
            redirectAttributes.addFlashAttribute("request", sanitizeOrEmpty(request));
            return "redirect:/products/form-add";
        } catch (Exception e) {
            String errorCode = nextErrorCode();
            log.error("[Product][{}] Lỗi không xác định khi thêm sản phẩm | type={} | rootCause={}",
                    errorCode, e.getClass().getName(), getRootCauseMessage(e), e);
            redirectAttributes.addFlashAttribute("error", "Lỗi hệ thống, vui lòng thử lại. Mã lỗi: " + errorCode);
            redirectAttributes.addFlashAttribute("request", sanitizeOrEmpty(request));
            return "redirect:/products/form-add";
        }
    }

    @GetMapping("/{id}/form-edit")
    public String editProductForm(@PathVariable Long id, Model model,
            RedirectAttributes redirectAttributes) {
        try {
            if (!model.containsAttribute("request")) {
                model.addAttribute("request", productService.getProductRequestById(id));
            }
            model.addAttribute("productId", id);
            model.addAttribute("isEdit", true);
            return "product/add-product";
        } catch (IllegalArgumentException e) {
            log.warn("[Product] Không tìm thấy sản phẩm để edit - id={}", id);
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/products/list";
        }
    }

    @PostMapping("/{id}/update")
    public String updateProduct(@PathVariable Long id,
            @ModelAttribute("request") ProductRequestDTO request,
            RedirectAttributes redirectAttributes) {
        try {
            productService.updateProduct(id, request);
            log.info("[Product] Cập nhật sản phẩm thành công - id={}", id);
            redirectAttributes.addFlashAttribute("success", "Cập nhật sản phẩm thành công.");
            return "redirect:/products/list";
        } catch (IllegalArgumentException e) {
            log.warn("[Product] Lỗi validate khi cập nhật sản phẩm - {}", e.getMessage());
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            redirectAttributes.addFlashAttribute("request", sanitizeOrEmpty(request));
            return "redirect:/products/" + id + "/form-edit";
        } catch (MaxUploadSizeExceededException e) {
            log.warn("[Product] File ảnh vượt quá dung lượng khi cập nhật sản phẩm - id={}", id, e);
            redirectAttributes.addFlashAttribute("error", "Ảnh tải lên quá lớn. Vui lòng chọn file nhỏ hơn.");
            redirectAttributes.addFlashAttribute("request", sanitizeOrEmpty(request));
            return "redirect:/products/" + id + "/form-edit";
        } catch (DataIntegrityViolationException e) {
            String errorCode = nextErrorCode();
            log.error("[Product][{}] Lỗi ràng buộc dữ liệu khi cập nhật sản phẩm - id={} | rootCause={}",
                    errorCode, id, getRootCauseMessage(e), e);
            redirectAttributes.addFlashAttribute("error", "Dữ liệu không hợp lệ hoặc bị trùng. Mã lỗi: " + errorCode);
            redirectAttributes.addFlashAttribute("request", sanitizeOrEmpty(request));
            return "redirect:/products/" + id + "/form-edit";
        } catch (DataAccessException e) {
            String errorCode = nextErrorCode();
            log.error("[Product][{}] Lỗi truy cập dữ liệu khi cập nhật sản phẩm - id={} | rootCause={}",
                    errorCode, id, getRootCauseMessage(e), e);
            redirectAttributes.addFlashAttribute("error", "Lỗi cơ sở dữ liệu. Mã lỗi: " + errorCode);
            redirectAttributes.addFlashAttribute("request", sanitizeOrEmpty(request));
            return "redirect:/products/" + id + "/form-edit";
        } catch (Exception e) {
            String errorCode = nextErrorCode();
            log.error("[Product][{}] Lỗi không xác định khi cập nhật sản phẩm - id={} | type={} | rootCause={}",
                    errorCode, id, e.getClass().getName(), getRootCauseMessage(e), e);
            redirectAttributes.addFlashAttribute("error", "Lỗi hệ thống, vui lòng thử lại. Mã lỗi: " + errorCode);
            redirectAttributes.addFlashAttribute("request", sanitizeOrEmpty(request));
            return "redirect:/products/" + id + "/form-edit";
        }
    }

    @PostMapping("/{id}/delete")
    public String deleteProduct(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            productService.deleteProduct(id);
            log.info("[Product] Xóa sản phẩm thành công - id={}", id);
            redirectAttributes.addFlashAttribute("success", "Xóa sản phẩm thành công.");
        } catch (IllegalArgumentException e) {
            log.warn("[Product] Không tìm thấy sản phẩm để xóa - id={}", id);
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        } catch (DataAccessException e) {
            String errorCode = nextErrorCode();
            log.error("[Product][{}] Lỗi truy cập dữ liệu khi xóa sản phẩm - id={} | rootCause={}",
                    errorCode, id, getRootCauseMessage(e), e);
            redirectAttributes.addFlashAttribute("error", "Lỗi cơ sở dữ liệu. Mã lỗi: " + errorCode);
        } catch (Exception e) {
            String errorCode = nextErrorCode();
            log.error("[Product][{}] Lỗi không xác định khi xóa sản phẩm - id={} | type={} | rootCause={}",
                    errorCode, id, e.getClass().getName(), getRootCauseMessage(e), e);
            redirectAttributes.addFlashAttribute("error", "Lỗi hệ thống, vui lòng thử lại. Mã lỗi: " + errorCode);
        }
        return "redirect:/products/list";
    }

    private String nextErrorCode() {
        return "PRD-" + System.currentTimeMillis();
    }

    private String getRootCauseMessage(Throwable throwable) {
        Throwable root = throwable;
        while (root.getCause() != null) {
            root = root.getCause();
        }
        String message = root.getMessage();
        return message == null || message.isBlank() ? root.getClass().getName() : message;
    }

    private ProductRequestDTO sanitizeOrEmpty(ProductRequestDTO request) {
        if (request == null) {
            return new ProductRequestDTO();
        }
        return productService.sanitize(request);
    }
}