package fpt.legendcoffee.controller;

import fpt.legendcoffee.entity.Wallet;
import fpt.legendcoffee.entity.WalletTransaction;
import fpt.legendcoffee.service.AdminService;
import fpt.legendcoffee.service.WalletService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

/**
 * Controller dành cho Admin để quản lý ví hệ thống và phê duyệt các yêu cầu rút tiền.
 */
@Controller
@RequestMapping("/admin/wallet")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminWithdrawalController {

    private final AdminService adminService;
    private final WalletService walletService;

    /**
     * Hiển thị trang quản lý tài chính admin, bao gồm số dư và các yêu cầu rút tiền.
     */
    @GetMapping
    public String adminWallet(Model model) {
        Wallet adminWallet = walletService.getAdminWallet();
        model.addAttribute("adminWallet", adminWallet);

        List<WalletTransaction> pendingWithdrawals = adminService.getPendingWithdrawals();
        model.addAttribute("pendingWithdrawals", pendingWithdrawals);

        return "admin/wallet";
    }

    /**
     * Hiển thị danh sách các yêu cầu rút tiền đang chờ xử lý.
     */
    @GetMapping("/requests")
    public String listRequests(Model model) {
        List<WalletTransaction> pendingList = adminService.getPendingWithdrawals();
        model.addAttribute("withdrawals", pendingList);
        return "admin/wallet-requests";
    }

    /**
     * Phê duyệt yêu cầu rút tiền.
     */
    @PostMapping("/{id}/approve")
    public String approveWithdrawal(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            adminService.approveWithdrawal(id);
            redirectAttributes.addFlashAttribute("successMessage", "Đã phê duyệt giao dịch thành công.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi khi phê duyệt: " + e.getMessage());
        }
        return "redirect:/admin/wallet";
    }

    /**
     * Từ chối yêu cầu rút tiền (có hoàn tiền).
     */
    @PostMapping("/{id}/reject")
    public String rejectWithdrawal(@PathVariable Long id, 
                                   @RequestParam String note, 
                                   RedirectAttributes redirectAttributes) {
        try {
            adminService.rejectWithdrawal(id, note);
            redirectAttributes.addFlashAttribute("successMessage", "Đã từ chối giao dịch và hoàn tiền cho khách hàng.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi khi từ chối: " + e.getMessage());
        }
        return "redirect:/admin/wallet";
    }
}
