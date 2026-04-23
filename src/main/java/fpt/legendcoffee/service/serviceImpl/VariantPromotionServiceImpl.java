//package fpt.legendcoffee.service.serviceImpl;
//
//import fpt.legendcoffee.dto.response.PromotionDTO;
//import fpt.legendcoffee.entity.ProductVariant;
//import fpt.legendcoffee.entity.Promotion;
//import fpt.legendcoffee.entity.VariantPromotion;
//import fpt.legendcoffee.repository.VariantPromotionRepository;
//import fpt.legendcoffee.service.VariantPromotionService;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Service;
//
//import java.util.ArrayList;
//import java.util.List;
//
//@Service
//public class VariantPromotionServiceImpl implements VariantPromotionService {
//    @Autowired
//    private VariantPromotionRepository variantPromotionRepository;
//
////    @Override
////    public void assignPromotion(Long promotionId, Long variantId) {
////        public void assignPromotion (Long promotionId, Long variantId){
////
////            if (variantPromotionRepository.existsByPromotionIdAndVariantId(promotionId, variantId)) {
////                throw new RuntimeException("Đã tồn tại mapping");
////            }
////
////            Promotion p = promotionRepository.findById(promotionId)
////                    .orElseThrow(() -> new RuntimeException("Promotion không tồn tại"));
////
////            ProductVariant v = variantRepository.findById(variantId)
////                    .orElseThrow(() -> new RuntimeException("Variant không tồn tại"));
////
////            VariantPromotion vp = new VariantPromotion();
////            vp.setPromotion(p);
////            vp.setVariant(v);
////
////            variantPromotionRepository.save(vp);
////        }
////
////        @Override
////        public void removePromotion (Long promotionId, Long variantId){
////            VariantPromotion vp = variantPromotionRepository
////                    .findByPromotionIdAndVariantId(promotionId, variantId)
////                    .orElseThrow(() -> new RuntimeException("Không tìm thấy mapping"));
////
////            variantPromotionRepository.delete(vp);
////        }
////
////        @Override
////        public List<PromotionDTO> getPromotionsByVariant (Long variantId){
////            List<VariantPromotion> list =
////                    variantPromotionRepository.findByVariantId(variantId);
////
////            List<Promotion> result = new ArrayList<>();
////
////            for (VariantPromotion vp : list) {
////                result.add(vp.getPromotion());
////            }
////
////            return result;
////        }
//    }
