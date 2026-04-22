package fpt.legendcoffee.controller;

import fpt.legendcoffee.common.security.CustomUserDetails;
import fpt.legendcoffee.dto.response.ProductResponseDTO;
import fpt.legendcoffee.service.ProductService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class HomeController {

    private final ProductService productService;

    @GetMapping({ "/","/home"})
    public String index(Model model,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse) {
        List<ProductResponseDTO> products = productService.getAllProductResponses();
        // Giới hạn hiển thị 3-6 sản phẩm nổi bật ở trang chủ
        if (products.size() > 6) {
            products = products.subList(0, 6);
        }
        model.addAttribute("products", products);
        Authentication authenticationResponse = SecurityContextHolder.getContext().getAuthentication();

        if (authenticationResponse.getPrincipal() instanceof CustomUserDetails userDetails) {
            boolean isAdmin = userDetails.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
            return isAdmin ? "redirect:/admin" : "redirect:/index";
        }
        return "redirect:/login";
    }
}
