package fpt.legendcoffee.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

//    @GetMapping("/wallet")
//    public String customerWallet(Model model) {
//        Map<String, Object> wallet = new HashMap<>();
//        wallet.put("id", 12345);
//        wallet.put("balance", 5000000.0);
//
//        List<Map<String, Object>> history = new ArrayList<>();
//        history.add(createTransaction("Nạp tiền", "+1,000,000", "Nạp qua VNPay", 1000000,
//                LocalDateTime.now().minusHours(2)));
//        history.add(createTransaction("Thanh toán", "-500,000", "Thanh toán Đơn hàng #101", -500000,
//                LocalDateTime.now().minusDays(1)));
//
//        wallet.put("transactionHistory", history);
//        model.addAttribute("wallet", wallet);
//        return "wallet/wallet";
//    }

    @GetMapping("/admin/wallet")
    public String adminWallet(Model model) {
        Map<String, Object> adminWallet = new HashMap<>();
        adminWallet.put("balance", 150000000.0);

        List<Map<String, Object>> history = new ArrayList<>();
        // Today's Work History (22-04-2026)
        history.add(createTransaction("Sửa lỗi Cloudinary", "+100,000", "Fix Missing Permissions (actions=['create'])",
                100000,
                LocalDateTime.of(2026, 4, 22, 13, 15)));
        history.add(createTransaction("Sửa lỗi điều hướng", "+50,000", "Fix MethodArgumentTypeMismatchException", 50000,
                LocalDateTime.of(2026, 4, 22, 13, 30)));
        history.add(createTransaction("Fix HomeController", "+50,000", "Fix Product null on homepage", 50000,
                LocalDateTime.of(2026, 4, 22, 13, 45)));
        history.add(createTransaction("Cập nhật Profile", "+200,000", "Implement editProfile & ProfileDTO", 200000,
                LocalDateTime.of(2026, 4, 22, 14, 15)));
        history.add(createTransaction("UI/UX Enhancements", "+150,000", "Add Edit Mode Toggle (JS)", 150000,
                LocalDateTime.of(2026, 4, 22, 15, 40)));
        history.add(createTransaction("Tiện ích Ví tiền", "+300,000", "Add Wallet buttons & Wallet Layouts", 300000,
                LocalDateTime.of(2026, 4, 22, 16, 35)));
        history.add(createTransaction("Sửa lỗi UI Wallet", "+50,000", "Fix missing 'username' in Admin Wallet", 50000,
                LocalDateTime.now().minusMinutes(10)));

        adminWallet.put("transactionHistory", history);
        model.addAttribute("adminWallet", adminWallet);
        return "admin/wallet";
    }

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
}
