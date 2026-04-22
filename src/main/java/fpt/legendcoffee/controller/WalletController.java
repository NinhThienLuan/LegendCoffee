package fpt.legendcoffee.controller;

import fpt.legendcoffee.common.util.SecurityUtils;
import fpt.legendcoffee.dto.request.WithDrawRequestDTO;
import fpt.legendcoffee.entity.enumeration.BankCode;
import fpt.legendcoffee.entity.WalletTransaction;
import fpt.legendcoffee.service.WalletService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.math.BigDecimal;

/**
 * Controller quản lý các hoạt động liên quan đến ví (Wallet),
 * bao gồm nạp tiền, rút tiền và xem số dư.
 */
@Controller
@RequestMapping("/wallet")
@RequiredArgsConstructor
public class WalletController {

    private final WalletService walletService;

    /**
     * Hiển thị trang chính của ví (Số dư + Lịch sử giao dịch).
     */
    @GetMapping
    public String showWallet(Model model) {
        Long userId = SecurityUtils.getCurrentUserId();
        if (userId == null) {
            return "redirect:/login";
        }

        BigDecimal balance = walletService.getWalletBalance(userId);
        List<WalletTransaction> history = walletService.getTransactionsByUserId(userId);

        model.addAttribute("balance", balance);
        model.addAttribute("history", history);

        return "wallet/wallet";
    }

    // --- Rút tiền ---

    /**
     * Hiển thị form rút tiền.
     */
    @GetMapping("/withdraw")
    public String showWithdrawForm(Model model) {
        if (!model.containsAttribute("withdrawRequest")) {
            model.addAttribute("withdrawRequest", new WithDrawRequestDTO());
        }
        model.addAttribute("bankCodes", BankCode.values());
        return "wallet/withdraw";
    }

    /**
     * Xử lý yêu cầu rút tiền.
     */
    @PostMapping("/withdraw")
    public String processWithdraw(@Valid @ModelAttribute("withdrawRequest") WithDrawRequestDTO withdrawRequest,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("bankCodes", BankCode.values());
            return "wallet/withdraw";
        }

        try {
            walletService.requestWithdrawal(withdrawRequest);
            redirectAttributes.addFlashAttribute("successMessage", "Yêu cầu rút tiền đã được gửi thành công!");
            return "redirect:/wallet";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Rút tiền thất bại: " + e.getMessage());
            return "redirect:/wallet/withdraw";
        }
    }
}
