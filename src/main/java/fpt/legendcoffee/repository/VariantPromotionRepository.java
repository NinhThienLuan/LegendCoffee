package fpt.legendcoffee.repository;

import fpt.legendcoffee.entity.VariantPromotion;
import fpt.legendcoffee.entity.ProductVariant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VariantPromotionRepository extends JpaRepository<VariantPromotion, Long> {
    boolean existsByPromotionIdAndVariantId(Long promotionId, Long variantId);
    VariantPromotion findByPromotionIdAndVariantId(Long promotionId, Long variantId);
    List<VariantPromotion> findByVariantId(Long variantId);
    List<VariantPromotion> findByPromotionId(Long promotionId);
    List<VariantPromotion> findByVariant(ProductVariant variant);
}
