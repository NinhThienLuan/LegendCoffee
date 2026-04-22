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
    private final fpt.legendcoffee.service.ArticleService articleService;

    @GetMapping({ "/", "/home", "/index"})
    public String index(Model model) {
        List<ProductResponseDTO> products = productService.getAllProductResponses();

        if (products.size() > 3) {
            products = products.subList(0, 3);
        }
        model.addAttribute("products", products);

        List<fpt.legendcoffee.entity.Article> articles = articleService.getPublishedArticles();
        if (articles.size() > 3) {
            articles = articles.subList(0, 3);
        }
        model.addAttribute("articles", articles);
        
        Authentication authenticationResponse = SecurityContextHolder.getContext().getAuthentication();

        if (authenticationResponse != null && authenticationResponse.getPrincipal() instanceof CustomUserDetails userDetails) {
            boolean isAdmin = userDetails.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
            if (isAdmin) {
                return "redirect:/admin";
            }
        }
        return "index";
    }
}
