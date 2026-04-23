//package fpt.legendcoffee.controller;
//
//import fpt.legendcoffee.service.VariantPromotionService;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Controller;
//
//@Controller
//public class VariantPromotionController {
//    @Autowired
//    private VariantPromotionService variantPromotionService;
//
////    // ASSIGN promotion cho variant
////    @PostMapping("/variant-promotions/assign")
////    public void assign(@RequestParam Long promotionId,
////                       @RequestParam Long variantId) {
////        variantPromotionService.assignPromotion(promotionId, variantId);
////    }
////
////    // REMOVE promotion khỏi variant
////    @DeleteMapping("/variant-promotions/remove")
////    public void remove(@RequestParam Long promotionId,
////                       @RequestParam Long variantId) {
////        variantPromotionService.removePromotion(promotionId, variantId);
////    }
////
////    // GET promotions theo variant
////    @GetMapping("/variant-promotions/variant/{variantId}")
////    public List<PromotionDTO> getByVariant(@PathVariable Long variantId) {
////        return variantPromotionService.getPromotionsByVariant(variantId);
////    }
////
////    // (optional) GET variants theo promotion
////    @GetMapping("/variant-promotions/promotion/{promotionId}")
////    public List<Long> getVariants(@PathVariable Long promotionId) {
////        return variantPromotionService.getVariantsByPromotion(promotionId);
////    }
//}
