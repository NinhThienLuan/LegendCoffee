package fpt.legendcoffee.service;

import fpt.legendcoffee.dto.response.ProductVariantDTO;
import fpt.legendcoffee.dto.response.PromotionDTO;
import fpt.legendcoffee.entity.ProductVariant;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface VariantPromotionService {
    void assignPromotion(Long promotionId, Long variantId);
    void removePromotion(Long promotionId, Long variantId);
    List<PromotionDTO> getPromotionsByVariant(Long variantId);
    List<ProductVariantDTO> getVariantsByPromotion(Long promotionId);
}
