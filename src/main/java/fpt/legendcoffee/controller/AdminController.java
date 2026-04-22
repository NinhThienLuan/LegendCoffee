package fpt.legendcoffee.controller;

import fpt.legendcoffee.dto.response.ProductResponseDTO;
import fpt.legendcoffee.service.ProductService;
import fpt.legendcoffee.service.ProductVariantService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminController {

    private final ProductVariantService productVariantService;
    private final ProductService productService;

    @GetMapping("")
    public String adminHome(Model model) {
        return "admin/dashboard";
    }

    /**
     * GET /admin/orders?status=PENDING
     * Redirects to /orders — the shared order page handles admin vs user view.
     */
    @GetMapping("/orders")
    public String adminOrders(@RequestParam(required = false) String status,
                              RedirectAttributes ra) {
        if (status != null && !status.isBlank()) {
            ra.addAttribute("status", status);
        }
        return "redirect:/orders";
    }

    /**
     * GET /admin/variants?keyword=&active=true
     * Renders admin/products with a filtered variants section at the bottom.
     */
    @GetMapping("/variants")
    public String adminVariants(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String active,
            Model model) {

        Boolean activeFilter = null;
        if ("true".equalsIgnoreCase(active)) activeFilter = true;
        else if ("false".equalsIgnoreCase(active)) activeFilter = false;

        // Populate products for the top table (same as /products for admin)
        List<ProductResponseDTO> products = productService.getAllProductResponses();
        model.addAttribute("products", products);
        model.addAttribute("totalProducts", productService.countProducts());
        model.addAttribute("activeProducts", productService.countActiveProducts());
        model.addAttribute("lowStockProducts", productService.countLowStockProducts());

        // Variants section
        model.addAttribute("variants", productVariantService.searchVariants(keyword, activeFilter));
        model.addAttribute("variantKeyword", keyword != null ? keyword : "");
        model.addAttribute("variantActiveFilter", active != null ? active : "");
        model.addAttribute("showVariants", true); // tells the template to scroll to variants
        return "products";
    }
}
