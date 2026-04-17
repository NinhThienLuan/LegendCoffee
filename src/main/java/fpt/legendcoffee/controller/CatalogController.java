package fpt.legendcoffee.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.ui.Model;

@Controller
public class CatalogController {

    // for view access only
    @GetMapping("/")
    public String getHome() {
        return "index";
    }

    // for view access only
    @GetMapping("/catalogs")
    public String getCatalogs() {
        return "catalogs";
    }

    // for view access only
    @GetMapping("/catalogs/{id}")
    public String getCatalogDetails(@PathVariable long id, Model model) {
        model.addAttribute("catalogId", id);
        return "catalog-detail";
    }

    // for view access only
    @GetMapping("/articles")
    public String getArticles() {
        return "editor-lab";
    }

    @GetMapping("/promotion")
    public String getPromotion() {
        return "admin/promotion";
    }

    // ── ADMIN PAGES (view access only) ────────────────────────────────────────

    @GetMapping("/dashboard")
    public String adminDashboard() {
        return "admin/dashboard";
    }

    @GetMapping("/users")
    public String adminUsers() {
        return "admin/users";
    }

    @GetMapping("/products")
    public String adminProducts() {
        return "admin/products";
    }

    @GetMapping("/posts")
    public String adminPosts() {
        return "admin/posts";
    }

}
