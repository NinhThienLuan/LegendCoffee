package fpt.legendcoffee.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import fpt.legendcoffee.dto.request.ProductRequestDTO;
import fpt.legendcoffee.entity.Product;
import fpt.legendcoffee.service.ProductService;
import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @GetMapping
    public String listProducts(Model model) {
        List<Product> products = productService.getAllProducts();
        model.addAttribute("products", products);
        return "product/products";
    }

    @GetMapping({"/view", "/list"})
    public String listProductsAlias(Model model) {
        return listProducts(model);
    }

    @GetMapping("/view-product")
    public String viewProductPage(Model model, RedirectAttributes redirectAttributes) {
        List<Product> products = productService.getAllProducts();
        if (products.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Chưa có sản phẩm để xem.");
            return "redirect:/products";
        }
        model.addAttribute("product", products.get(0));
        return "product/view-product";
    }

    @GetMapping("/{id}")
    public String viewProduct(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            model.addAttribute("product", productService.getProductById(id));
            return "product/view-product";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/products";
        }
    }

    @GetMapping("/form-add")
    public String addProductForm(Model model) {
        if (!model.containsAttribute("request")) {
            model.addAttribute("request", new ProductRequestDTO());
        }
        return "product/add-product";
    }

    @PostMapping("/add")
    public String addProduct(@ModelAttribute("request") ProductRequestDTO request,
            RedirectAttributes redirectAttributes) {
        try {
            productService.addProduct(request);
            redirectAttributes.addFlashAttribute("success", "Thêm sản phẩm thành công!");
            return "redirect:/products/form-add";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            redirectAttributes.addFlashAttribute("request", request);
            return "redirect:/products/form-add";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Có lỗi xảy ra khi thêm sản phẩm. Vui lòng thử lại.");
            redirectAttributes.addFlashAttribute("request", request);
            return "redirect:/products/form-add";
        }
    }

    @GetMapping("/{id}/form-edit")
    public String editProductForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            Product product = productService.getProductById(id);
            if (!model.containsAttribute("request")) {
                ProductRequestDTO request = new ProductRequestDTO();
                request.setName(product.getName());
                request.setDescription(product.getDescription());
                request.setOrigin(product.getOrigin());
                request.setManufacturerDate(product.getManufacturerDate());
                request.setExpiryDate(product.getExpiryDate());
                request.setIsActive(product.getIsActive());
                request.setCategoryId(product.getCategory() != null ? product.getCategory().getId() : null);
                model.addAttribute("request", request);
            }
            model.addAttribute("productId", id);
            model.addAttribute("currentImageUrl", product.getImageUrl());
            return "product/edit-product";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/products";
        }
    }

    @PostMapping("/{id}/update")
    public String updateProduct(@PathVariable Long id,
            @ModelAttribute("request") ProductRequestDTO request,
            RedirectAttributes redirectAttributes) {
        try {
            productService.updateProduct(id, request);
            redirectAttributes.addFlashAttribute("success", "Cập nhật sản phẩm thành công!");
            return "redirect:/products/" + id;
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            redirectAttributes.addFlashAttribute("request", request);
            return "redirect:/products/" + id + "/form-edit";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Có lỗi xảy ra khi cập nhật sản phẩm. Vui lòng thử lại.");
            redirectAttributes.addFlashAttribute("request", request);
            return "redirect:/products/" + id + "/form-edit";
        }
    }

    @PostMapping("/{id}/delete")
    public String deleteProduct(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            productService.deleteProduct(id);
            redirectAttributes.addFlashAttribute("success", "Xóa sản phẩm thành công!");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Có lỗi xảy ra khi xóa sản phẩm. Vui lòng thử lại.");
        }
        return "redirect:/products";
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public String handleMaxUploadSizeExceeded(RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("error", "File quá lớn! Vui lòng chọn file dưới 5MB.");
        return "redirect:/products/form-add";
    }
}
