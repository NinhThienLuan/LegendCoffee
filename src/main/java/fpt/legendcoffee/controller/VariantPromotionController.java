package fpt.legendcoffee.controller;

import fpt.legendcoffee.dto.response.ProductVariantDTO;
import fpt.legendcoffee.dto.response.PromotionDTO;
import fpt.legendcoffee.service.VariantPromotionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
public class VariantPromotionController {
    @Autowired
    private VariantPromotionService variantPromotionService;


    // ASSIGN
    @PostMapping
    public String assign(@RequestParam Long promotionId,
                         @RequestParam Long variantId) {

        variantPromotionService.assignPromotion(promotionId, variantId);
        return "Assign promotion thành công";
    }

    // REMOVE
    @DeleteMapping
    public String remove(@RequestParam Long promotionId,
                         @RequestParam Long variantId) {

        variantPromotionService.removePromotion(promotionId, variantId);
        return "Remove promotion thành công";
    }

    // GET promotions theo variant
    @GetMapping("/variant-promotions/variant/{variantId}")
    public List<PromotionDTO> getByVariant(@PathVariable Long variantId) {
        return variantPromotionService.getPromotionsByVariant(variantId);
    }

    // (optional) GET variants theo promotion
    @GetMapping("/variant-promotions/promotion/{promotionId}")
    public List<ProductVariantDTO> getVariants(@PathVariable Long promotionId) {
        return variantPromotionService.getVariantsByPromotion(promotionId);
    }
}
