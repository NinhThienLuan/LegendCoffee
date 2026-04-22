package fpt.legendcoffee.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

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

<<<<<<< Updated upstream
=======


    private Map<String, Object> createTransaction(String type, String amountStr, String desc, double amount,
            LocalDateTime date) {
        Map<String, Object> t = new HashMap<>();
        t.put("type", type);
        t.put("amountStr", amountStr);
        t.put("description", desc);
        t.put("amount", amount);
        t.put("createdAt", date);
        t.put("username", "Khanh Nguyễn");
        t.put("id", "TX-" + System.currentTimeMillis() % 1000000);
        return t;
    }
>>>>>>> Stashed changes
}
