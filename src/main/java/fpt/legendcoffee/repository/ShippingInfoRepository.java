package fpt.legendcoffee.repository;

import fpt.legendcoffee.entity.ShippingInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ShippingInfoRepository extends JpaRepository<ShippingInfo, Long> {

    Optional<ShippingInfo> findByOrderId(Long orderId);

    Optional<ShippingInfo> findByGhnOrderCode(String ghnOrderCode);

    boolean existsByOrderId(Long orderId);

    /**
     * Cập nhật trạng thái khi nhận webhook từ GHN
     */
    @Modifying
    @Query("UPDATE ShippingInfo s SET s.status = :status, s.updatedAt = CURRENT_TIMESTAMP " +
            "WHERE s.ghnOrderCode = :ghnOrderCode")
    int updateStatusByGhnOrderCode(@Param("ghnOrderCode") String ghnOrderCode,
                                   @Param("status") String status);
}
