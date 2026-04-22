package fpt.legendcoffee.controller;

import fpt.legendcoffee.entity.ProductVariant;
import fpt.legendcoffee.entity.Promotion;
import fpt.legendcoffee.entity.VariantPromotion;
import fpt.legendcoffee.repository.ProductVariantRepository;
import fpt.legendcoffee.repository.VariantPromotionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/promotions")
public class PromotionAPIController {

    private final ProductVariantRepository productVariantRepository;
    private final VariantPromotionRepository variantPromotionRepository;

    /**
     * Get promotions for a specific variant
     * GET /api/promotions/variant/{variantId}
     */
    @GetMapping("/variant/{variantId}")
    public ResponseEntity<?> getPromotionsForVariant(@PathVariable Long variantId) {
        log.info("[PromotionAPI] Getting promotions for variant: {}", variantId);

        Optional<ProductVariant> variantOpt = productVariantRepository.findById(variantId);
        if (variantOpt.isEmpty()) {
            log.warn("[PromotionAPI] Variant not found: {}", variantId);
            return ResponseEntity.ok(Collections.emptyList());
        }

        ProductVariant variant = variantOpt.get();
        List<VariantPromotion> variantPromotions = variantPromotionRepository.findByVariantId(variantId);

        List<Map<String, Object>> promotions = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();

        for (VariantPromotion vp : variantPromotions) {
            Promotion promo = vp.getPromotion();

            // Check if promotion is active (within date range)
            if (promo.getStartDate() != null && now.isBefore(promo.getStartDate())) {
                continue; // Not yet started
            }
            if (promo.getEndDate() != null && now.isAfter(promo.getEndDate())) {
                continue; // Expired
            }

            Map<String, Object> promoData = new HashMap<>();
            promoData.put("id", promo.getId());
            promoData.put("type", promo.getType());
            promoData.put("value", promo.getValue());
            promoData.put("startDate", promo.getStartDate());
            promoData.put("endDate", promo.getEndDate());

            promotions.add(promoData);
        }

        log.info("[PromotionAPI] Found {} active promotions for variant {}", promotions.size(), variantId);
        return ResponseEntity.ok(promotions);
    }

    /**
     * Calculate promotion discount for items
     * POST /api/promotions/calculate
     * Body: { "items": [{ "variantId": 1, "quantity": 1 }] }
     */
    @PostMapping("/calculate")
    public ResponseEntity<?> calculatePromotionDiscount(@RequestBody Map<String, Object> request) {
        List<Map<String, Object>> items = (List<Map<String, Object>>) request.get("items");
        BigDecimal totalDiscount = BigDecimal.ZERO;
        Map<String, Object> result = new HashMap<>();
        List<Map<String, Object>> itemDiscounts = new ArrayList<>();

        for (Map<String, Object> item : items) {
            Long variantId = Long.valueOf(item.get("variantId").toString());
            Integer quantity = Integer.valueOf(item.get("quantity").toString());
            BigDecimal price = new BigDecimal(item.get("price").toString());

            Optional<ProductVariant> variantOpt = productVariantRepository.findById(variantId);
            if (variantOpt.isEmpty()) continue;

            ProductVariant variant = variantOpt.get();
            List<VariantPromotion> variantPromotions = variantPromotionRepository.findByVariantId(variantId);

            BigDecimal itemDiscount = BigDecimal.ZERO;
            Promotion appliedPromo = null;

            LocalDateTime now = LocalDateTime.now();
            for (VariantPromotion vp : variantPromotions) {
                Promotion promo = vp.getPromotion();

                // Check date range
                if (promo.getStartDate() != null && now.isBefore(promo.getStartDate())) continue;
                if (promo.getEndDate() != null && now.isAfter(promo.getEndDate())) continue;

                // Calculate discount
                if ("PERCENT".equals(promo.getType())) {
                    itemDiscount = price.multiply(BigDecimal.valueOf(quantity))
                            .multiply(promo.getValue())
                            .divide(BigDecimal.valueOf(100), 2, java.math.RoundingMode.HALF_UP);
                } else if ("FIXED".equals(promo.getType())) {
                    itemDiscount = promo.getValue().multiply(BigDecimal.valueOf(quantity));
                }

                appliedPromo = promo;
                break; // Apply first active promotion only
            }

            totalDiscount = totalDiscount.add(itemDiscount);

            Map<String, Object> itemData = new HashMap<>();
            itemData.put("variantId", variantId);
            itemData.put("discount", itemDiscount);
            itemData.put("promotion", appliedPromo != null ? appliedPromo.getType() : null);
            itemDiscounts.add(itemData);
        }

        result.put("totalDiscount", totalDiscount);
        result.put("items", itemDiscounts);

        log.info("[PromotionAPI] Calculated total promotion discount: {}", totalDiscount);
        return ResponseEntity.ok(result);
    }
}

