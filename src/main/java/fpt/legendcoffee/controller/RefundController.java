package fpt.legendcoffee.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import fpt.legendcoffee.common.util.SecurityUtils;
import fpt.legendcoffee.dto.request.RefundRequestDTO;
import fpt.legendcoffee.service.RefundService;
import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/refunds")
@RequiredArgsConstructor
public class RefundController {

    private final RefundService refundService;

   
    @GetMapping("/request")
    @PreAuthorize("hasRole('USER')")
    public String showRefundForm(Model model) {
        
        model.addAttribute("refundRequest", new RefundRequestDTO());
        return "refund/request-form"; 
    }

  
    @PostMapping("/request")
    @PreAuthorize("hasRole('USER')")
    public String submitRefundRequest(@ModelAttribute RefundRequestDTO requestDTO, Model model) {
        Long currentUserId = SecurityUtils.getCurrentUserId();
        
 
        refundService.createRefundRequest(currentUserId, requestDTO.getOrderId(), requestDTO.getAmount(), requestDTO.getReason());
        
       
        model.addAttribute("message", "Gửi yêu cầu hoàn tiền thành công.");
        return "refund/success-page"; 
    }

    @PostMapping("/{id}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public String approveRefund(@PathVariable Long id, Model model) {
        refundService.approveRefund(id);
        
        return "redirect:/admin/refunds?success=approve"; 
    }

    @PostMapping("/{id}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    public String rejectRefund(@PathVariable Long id, Model model) {
        refundService.rejectRefund(id);
        
        return "redirect:/admin/refunds?success=reject"; 
    }
}