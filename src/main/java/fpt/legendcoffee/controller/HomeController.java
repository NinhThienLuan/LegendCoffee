package fpt.legendcoffee.controller;

import fpt.legendcoffee.dto.response.ProductResponseDTO;
import fpt.legendcoffee.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class HomeController {

    private final ProductService productService;

    @GetMapping("/")
    public String index(Model model) {
        List<ProductResponseDTO> products = productService.getAllProductResponses();

        if (products.size() > 3) {
            products = products.subList(0, 3);
        }
        model.addAttribute("products", products);
        return "index";
    }
}
