package fpt.legendcoffee.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import fpt.legendcoffee.dto.request.ProductRequestDTO;
import fpt.legendcoffee.dto.response.ProductDetailDTO;
import fpt.legendcoffee.dto.response.ProductResponseDTO;
import fpt.legendcoffee.entity.Product;
import fpt.legendcoffee.entity.ProductVariant;
import fpt.legendcoffee.service.ImageService;
import fpt.legendcoffee.service.ProductService;
import fpt.legendcoffee.service.ProductVariantService;
import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/products")
@RequiredArgsConstructor
public class ProductController {

    private static final Logger log = LoggerFactory.getLogger(ProductController.class);

    private final ProductService productService;
    private final ImageService imageService;
    private final ProductVariantService productVariantService;

    @PostMapping("/image-upload")
    public String uploadImageOnly(@RequestParam("image") MultipartFile image, Model model) {
        if (image == null || image.isEmpty()) {
            model.addAttribute("error", "Vui lòng chọn ảnh trước khi upload.");
            return "product/image-upload";
        }

        try {
            var uploadResult = imageService.upload(image);
            String imageUrl = (String) uploadResult.get("secure_url");
            String imagePublicId = (String) uploadResult.get("public_id");

            model.addAttribute("uploadedImageUrl", imageUrl);
            model.addAttribute("imagePublicId", imagePublicId);
            model.addAttribute("success", "Upload ảnh thành công.");
            return "product/image-upload";
        } catch (Exception e) {
            log.error("[Product] Upload ảnh thất bại | type={} | rootCause={}",
                    e.getClass().getName(), getRootCauseMessage(e), e);
            model.addAttribute("error", "Upload ảnh thất bại: " + getRootCauseMessage(e));
            return "product/image-upload";
        }
    }

    @GetMapping
    public String listProducts(Model model, Authentication authentication) {
        List<ProductResponseDTO> products = productService.getAllProductResponses();
        model.addAttribute("products", products);
        model.addAttribute("totalProducts", productService.countProducts());
        model.addAttribute("activeProducts", productService.countActiveProducts());
        model.addAttribute("lowStockProducts", productService.countLowStockProducts());
        if (authentication != null && authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ROLE_STAFF"))) {
            return "product/products";
        }
        model.addAttribute("pageType", "products");
        return "product/catalogs";
    }

    @GetMapping("/{id}")
    public String viewProduct(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            ProductDetailDTO product = productService.getProductDetail(id);
            List<ProductVariant> variants = productVariantService.getVariantsByProductId(id);
            ProductVariant selectedVariant = null;
            for (ProductVariant variant : variants) {
                if (Boolean.TRUE.equals(variant.getIsActive())) {
                    selectedVariant = variant;
                    break;
                }
            }
            if (selectedVariant == null && !variants.isEmpty()) {
                selectedVariant = variants.get(0);
            }

            model.addAttribute("productId", id);
            model.addAttribute("product", product);
            model.addAttribute("variants", variants);
            model.addAttribute("selectedVariant", selectedVariant);
            return "product/catalog-detail";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/products";
        }
    }

    @GetMapping("/form-add")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public String addProductForm(Model model) {
        Object requestAttr = model.getAttribute("request");
        if (!(requestAttr instanceof ProductRequestDTO)) {
            model.addAttribute("request", new ProductRequestDTO());
        }
        model.addAttribute("categories", productService.getAllCategories());
        model.addAttribute("editMode", false);
        return "product/product-form";
    }

    @PostMapping("/add")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public String addProduct(@ModelAttribute("request") ProductRequestDTO request,
            RedirectAttributes redirectAttributes) {
        try {
            productService.addProduct(request);
            log.info("[Product] Thêm sản phẩm thành công - name={}", request.getName());
            redirectAttributes.addFlashAttribute("success", "Thêm sản phẩm thành công.");
            return "redirect:/products";
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
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public String editProductForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            Product product = productService.getProductById(id);

            if (!model.containsAttribute("request")) {
                model.addAttribute("request", productService.getProductRequestById(id));
            }

            List<ProductVariant> existingVariants = productVariantService.findByProduct(product);

            model.addAttribute("productId", id);
            model.addAttribute("currentImageUrl", product.getImageUrl());
            model.addAttribute("existingVariants", existingVariants);
            model.addAttribute("categories", productService.getAllCategories());
            model.addAttribute("editMode", true);
            return "product/product-form";
        } catch (IllegalArgumentException e) {
            log.warn("[Product] Không tìm thấy sản phẩm để edit - id={}", id);
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/products";
        }
    }

    @PostMapping("/{id}/update")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public String updateProduct(@PathVariable Long id,
            @ModelAttribute("request") ProductRequestDTO request,
            RedirectAttributes redirectAttributes) {
        try {
            productService.updateProduct(id, request);
            log.info("[Product] Cập nhật sản phẩm thành công - id={}", id);
            redirectAttributes.addFlashAttribute("success", "Cập nhật sản phẩm thành công.");
            return "redirect:/products";
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
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
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
        return "redirect:/products";
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
