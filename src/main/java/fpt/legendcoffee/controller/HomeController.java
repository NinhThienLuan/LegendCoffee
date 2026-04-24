package fpt.legendcoffee.controller;

import fpt.legendcoffee.dto.response.ProductResponseDTO;
import fpt.legendcoffee.service.ArticleService;
import fpt.legendcoffee.service.ProductService;
import lombok.RequiredArgsConstructor;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class HomeController {

    private final ProductService productService;
    private final ArticleService articleService;

    @GetMapping({ "/","/index","/home" })
    public String index(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            return "redirect:/admin";
        }

        List<fpt.legendcoffee.entity.Article> articles = articleService.getPublishedArticles();
        if (articles.size() > 3) {
            articles = articles.subList(0, 3);
        }
        model.addAttribute("articles", articles);
        List<ProductResponseDTO> products = productService.searchProducts(null, true);
        if (products.size() > 3) {
            products = products.subList(0, 3);
        }
        model.addAttribute("products", products);
        return "index";
    }
}
