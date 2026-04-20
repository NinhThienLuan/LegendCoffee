package fpt.legendcoffee.repository;

import fpt.legendcoffee.entity.VariantPromotion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VariantPromotionRepository extends JpaRepository<VariantPromotion, Long> {
}
