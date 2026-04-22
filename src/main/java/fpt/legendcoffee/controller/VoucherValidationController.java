package fpt.legendcoffee.controller;

import fpt.legendcoffee.entity.Voucher;
import fpt.legendcoffee.entity.enumeration.VoucherType;
import fpt.legendcoffee.repository.VoucherRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/vouchers")
public class VoucherValidationController {

    private final VoucherRepository voucherRepository;

    /**
     * Validate voucher code & calculate discount
     * Request: GET /api/vouchers/validate?code=WELCOME2024&amount=370000
     */
    @GetMapping("/validate")
    public ResponseEntity<?> validateVoucher(
            @RequestParam String code,
            @RequestParam BigDecimal amount) {
        
        log.info("[VoucherValidation] Validating voucher: {} with amount: {}", code, amount);

        Map<String, Object> response = new HashMap<>();
        
        // Find voucher
        Optional<Voucher> voucherOpt = voucherRepository.findByCode(code.toUpperCase());
        if (voucherOpt.isEmpty()) {
            log.warn("[VoucherValidation] Voucher not found: {}", code);
            response.put("valid", false);
            response.put("message", "Mã voucher không tồn tại");
            return ResponseEntity.ok(response);
        }

        Voucher voucher = voucherOpt.get();
        LocalDateTime now = LocalDateTime.now();

        // Check active
        if (!Boolean.TRUE.equals(voucher.getActive())) {
            log.warn("[VoucherValidation] Voucher not active: {}", code);
            response.put("valid", false);
            response.put("message", "Mã voucher không hoạt động");
            return ResponseEntity.ok(response);
        }

        // Check date range
        if (voucher.getStartDate() != null && now.isBefore(voucher.getStartDate())) {
            log.warn("[VoucherValidation] Voucher not yet valid: {}", code);
            response.put("valid", false);
            response.put("message", "Mã voucher chưa đến thời gian sử dụng");
            return ResponseEntity.ok(response);
        }

        if (voucher.getEndDate() != null && now.isAfter(voucher.getEndDate())) {
            log.warn("[VoucherValidation] Voucher expired: {}", code);
            response.put("valid", false);
            response.put("message", "Mã voucher đã hết hạn");
            return ResponseEntity.ok(response);
        }

        // Check quota
        if (voucher.getUsageLimit() != null && voucher.getUsedCount() != null &&
            voucher.getUsedCount() >= voucher.getUsageLimit()) {
            log.warn("[VoucherValidation] Voucher usage limit reached: {}", code);
            response.put("valid", false);
            response.put("message", "Mã voucher đã hết quota");
            return ResponseEntity.ok(response);
        }

        // Check minimum condition
        if (voucher.getConditionMin() != null &&
            amount.compareTo(voucher.getConditionMin()) < 0) {
            log.warn("[VoucherValidation] Order amount {} < condition min {}", amount, voucher.getConditionMin());
            response.put("valid", false);
            response.put("message", "Đơn hàng chưa đạt giá trị tối thiểu " + voucher.getConditionMin() + "₫");
            return ResponseEntity.ok(response);
        }

        // Calculate discount
        BigDecimal discount = BigDecimal.ZERO;
        if ("PERCENT".equals(voucher.getType().name())) {
            discount = amount.multiply(voucher.getValue())
                    .divide(BigDecimal.valueOf(100), 2, java.math.RoundingMode.HALF_UP);
        } else if ("FIXED".equals(voucher.getType().name())) {
            discount = voucher.getValue();
        }

        log.info("[VoucherValidation] Voucher valid: {}, discount: {}", code, discount);
        response.put("valid", true);
        response.put("code", code);
        response.put("type", voucher.getType().name());
        response.put("value", voucher.getValue());
        response.put("discount", discount);
        response.put("message", "Voucher hợp lệ");

        return ResponseEntity.ok(response);
    }
}

