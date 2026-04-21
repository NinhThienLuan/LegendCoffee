package fpt.legendcoffee.controller;

import fpt.legendcoffee.entity.Article;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.ui.Model;

@Controller
public class TestController {

    @GetMapping("/a/promotion")
    public String getPromotion() {
        return "admin/promotion";
    }

    @GetMapping("/a/dashboard")
    public String adminDashboard() {
        return "admin/dashboard";
    }

    @GetMapping("/a/users")
    public String adminUsers() {
        return "admin/users";
    }

    @GetMapping("/a/products")
    public String adminProducts() {
        return "admin/products";
    }

    @GetMapping("/a/posts")
    public String adminPosts() {
        return "admin/posts";
    }

    @GetMapping("/a/post-form")
    public String adminPostForm(Model model) {
        model.addAttribute("article", new Article());
        return "admin/article-form";
    }

}
