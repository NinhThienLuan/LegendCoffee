package fpt.legendcoffee.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import fpt.legendcoffee.common.util.SecurityUtils;
import fpt.legendcoffee.dto.request.RefundRequestDTO;
import fpt.legendcoffee.service.RefundService;
import lombok.RequiredArgsConstructor;

@Controller("/refund")
@RequiredArgsConstructor
public class RefundController {

    private final RefundService refundService;

   
    @PostMapping("/request")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<String> createRequest(@RequestBody RefundRequestDTO requestDTO) {
      
    Long currentUserId = SecurityUtils.getCurrentUserId();
        refundService.createRefundRequest(currentUserId, requestDTO.getOrderId(), requestDTO.getAmount(), requestDTO.getReason());
        return ResponseEntity.ok("Gửi yêu cầu hoàn tiền thành công.");
    }

    
    @PutMapping("/{id}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> approve(@PathVariable Long id) {
        refundService.approveRefund(id);
        return ResponseEntity.ok("Admin đã duyệt và hoàn tiền thành công.");
    }

 
    @PutMapping("/{id}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> reject(@PathVariable Long id) {
        refundService.rejectRefund(id);
        return ResponseEntity.ok("Admin đã từ chối yêu cầu.");
    }
}