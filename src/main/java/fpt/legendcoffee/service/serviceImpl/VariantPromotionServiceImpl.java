package fpt.legendcoffee.service.serviceImpl;

import fpt.legendcoffee.dto.response.ProductVariantDTO;
import fpt.legendcoffee.dto.response.PromotionDTO;
import fpt.legendcoffee.entity.ProductVariant;
import fpt.legendcoffee.entity.Promotion;
import fpt.legendcoffee.entity.VariantPromotion;
import fpt.legendcoffee.repository.ProductVariantRepository;
import fpt.legendcoffee.repository.PromotionRepository;
import fpt.legendcoffee.repository.VariantPromotionRepository;
import fpt.legendcoffee.service.VariantPromotionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class VariantPromotionServiceImpl implements VariantPromotionService {

    @Autowired
    private VariantPromotionRepository variantPromotionRepository;

    @Autowired
    private PromotionRepository promotionRepository;

    @Autowired
    private ProductVariantRepository productVariantRepository;

    @Override
    public void assignPromotion(Long promotionId, Long variantId) {

        if (variantPromotionRepository.existsByPromotionIdAndVariantId(promotionId, variantId)) {
            throw new RuntimeException("Đã tồn tại mapping");
        }

        Promotion p = promotionRepository.findById(promotionId)
                .orElseThrow(() -> new RuntimeException("Promotion không tồn tại"));

        ProductVariant v = productVariantRepository.findById(variantId)
                .orElseThrow(() -> new RuntimeException("Variant không tồn tại"));

        VariantPromotion vp = new VariantPromotion();
        vp.setPromotion(p);
        vp.setVariant(v);

        variantPromotionRepository.save(vp);
    }

    @Override
    public void removePromotion(Long promotionId, Long variantId) {

        VariantPromotion vp = variantPromotionRepository
                .findByPromotionIdAndVariantId(promotionId, variantId);

        if (vp == null) {
            throw new RuntimeException("Mapping không tồn tại");
        }

        variantPromotionRepository.delete(vp);
    }

    @Override
    public List<PromotionDTO> getPromotionsByVariant(Long variantId) {

        List<VariantPromotion> list =
                variantPromotionRepository.findByVariantId(variantId);

        List<PromotionDTO> result = new ArrayList<>();

        for (VariantPromotion vp : list) {

            Promotion p = vp.getPromotion();

            // chỉ cần id, type, value
            PromotionDTO dto = new PromotionDTO();
            dto.setId(p.getId());
            dto.setType(p.getType());
            dto.setValue(p.getValue());

            result.add(dto);
        }

        return result;
    }

    @Override
    public List<ProductVariantDTO> getVariantsByPromotion(Long promotionId) {

        List<VariantPromotion> list =
                variantPromotionRepository.findByPromotionId(promotionId);

        List<ProductVariantDTO> result = new ArrayList<>();

        for (VariantPromotion vp : list) {

            ProductVariant v = vp.getVariant();

            ProductVariantDTO dto = new ProductVariantDTO();
            dto.setId(v.getId());
            dto.setVariantName(v.getVariantName());
            dto.setPrice(v.getPrice());
            dto.setImageUrl(v.getImageUrl());

            result.add(dto);
        }

        return result;
    }
}