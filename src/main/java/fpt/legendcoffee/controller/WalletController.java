package fpt.legendcoffee.controller;

import fpt.legendcoffee.dto.app.WithdrawalRequestDTO;
import fpt.legendcoffee.entity.User;
import fpt.legendcoffee.entity.Wallet;
import fpt.legendcoffee.entity.WithdrawalRequest;
import fpt.legendcoffee.service.UserService;
import fpt.legendcoffee.service.WalletService;
import fpt.legendcoffee.service.WithdrawalService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

@Slf4j
@Controller
@RequiredArgsConstructor
public class WalletController {

    private final WalletService walletService;
    private final WithdrawalService withdrawalService;
    private final UserService userService;

    // =========================================================================
    // User — Trang ví & yêu cầu rút tiền
    // =========================================================================

    @GetMapping("/wallet")
    public String walletPage(Model model) {
        Optional<User> currentUser = getCurrentUser();
        if (currentUser.isEmpty())
            return "redirect:/login";

        Long userId = currentUser.get().getId();
        boolean isAdmin = isCurrentUserAdmin();

        if (isAdmin) {
            return "redirect:/admin/wallet";
        }

        Wallet wallet = walletService.getWallet(userId);
        model.addAttribute("wallet", wallet);
        model.addAttribute("isAdmin", false);

        // User: xem số dư + form rút tiền + lịch sử yêu cầu
        List<WithdrawalRequest> myRequests = withdrawalService.getRequestsByUser(userId);
        model.addAttribute("myRequests", myRequests);
        model.addAttribute("withdrawalRequest", new WithdrawalRequestDTO());
        return "wallet/wallet";
    }

    // =========================================================================
    // Admin — Quản lý yêu cầu rút tiền
    // =========================================================================

    /**
     * GET /admin/wallet
     * Admin xem tất cả yêu cầu rút tiền.
     */
    @GetMapping("/admin/wallet")
    @PreAuthorize("hasRole('ADMIN')")
    public String adminWalletPage(Model model) {
        Optional<User> currentUser = getCurrentUser();
        if (currentUser.isEmpty())
            return "redirect:/login";

        Wallet wallet = walletService.getWallet(currentUser.get().getId());
        List<WithdrawalRequest> allRequests = withdrawalService.getAllRequests();

        model.addAttribute("wallet", wallet);
        model.addAttribute("withdrawalRequests", allRequests);
        return "admin/wallet";
    }

    /**
     * POST /admin/withdrawals/{id}/approve
     * Admin chấp nhận yêu cầu rút tiền.
     */
    @PostMapping("/admin/withdrawals/{id}/approve")
    public String approveWithdrawal(
            @PathVariable Long id,
            RedirectAttributes redirectAttributes) {
        try {
            WithdrawalRequest result = withdrawalService.approveWithdrawal(id);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Đã duyệt yêu cầu rút " + result.getAmount() + " ₫ của " + result.getUser().getUsername());
        } catch (Exception e) {
            log.error("[Admin][Withdrawal] Approve thất bại requestId={}: {}", id, e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
        }
        return "redirect:/admin/withdrawals";
    }

    /**
     * POST /admin/withdrawals/{id}/reject
     * Admin từ chối yêu cầu rút tiền kèm lý do.
     */
    @PostMapping("/admin/withdrawals/{id}/reject")
    public String rejectWithdrawal(
            @PathVariable Long id,
            @RequestParam(name = "reason") String reason,
            RedirectAttributes redirectAttributes) {
        try {
            withdrawalService.rejectWithdrawal(id, reason);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Đã từ chối yêu cầu rút tiền #" + id);
        } catch (Exception e) {
            log.error("[Admin][Withdrawal] Reject thất bại requestId={}: {}", id, e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
        }
        return "redirect:/admin/withdrawals";
    }

    // =========================================================================
    // Helper
    // =========================================================================

    private Optional<User> getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return Optional.empty();
        }
        return userService.findByEmail(auth.getName());
    }

    private boolean isCurrentUserAdmin() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null)
            return false;
        return auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }
}
