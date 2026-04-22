package fpt.legendcoffee.controller;

import fpt.legendcoffee.entity.Wallet;
import fpt.legendcoffee.service.WalletService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/admin/wallets")
@RequiredArgsConstructor
public class WalletAdminController {
    private final WalletService walletService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public String listWallets(Model model) {
        List<Wallet> wallets = walletService.getAllWallets();
        model.addAttribute("wallets", wallets);
        return "admin/wallets";
    }
}
