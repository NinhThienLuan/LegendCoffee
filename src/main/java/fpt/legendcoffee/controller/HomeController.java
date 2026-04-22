package fpt.legendcoffee.controller;

import fpt.legendcoffee.dto.response.ProductResponseDTO;
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

    @GetMapping({ "/"})
    public String index(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            return "redirect:/admin";
        }

        List<ProductResponseDTO> products = productService.getAllProductResponses();
        if (products.size() > 6) {
            products = products.subList(0, 6);
        }
        model.addAttribute("products", products);
        return "index";
    }
}
