package fpt.legendcoffee.repository;

import fpt.legendcoffee.entity.OrderItem;
import fpt.legendcoffee.entity.enumeration.OrderStatus;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
	long countByOrderId(Long orderId);

	@Query("""
			SELECT oi
			FROM OrderItem oi
			LEFT JOIN FETCH oi.variant v
			LEFT JOIN FETCH v.product p
			LEFT JOIN FETCH oi.combo c
			LEFT JOIN FETCH c.comboItems ci
			LEFT JOIN FETCH ci.variant cv
			LEFT JOIN FETCH cv.product cp
			WHERE oi.order.id = :orderId
			""")
	List<OrderItem> findByOrderIdWithDetails(@Param("orderId") Long orderId);

	@Query(value = """
			SELECT
			  (SELECT ISNULL(SUM(oi.quantity), 0)
			   FROM order_items oi
			   JOIN product_variants pv ON oi.variant_id = pv.id
			   JOIN orders o ON oi.order_id = o.id
			   WHERE pv.product_id = :productId AND o.status IN (:statuses))
			  +
			  (SELECT ISNULL(SUM(oi.quantity * ci.quantity), 0)
			   FROM order_items oi
			   JOIN combo_items ci ON oi.combo_id = ci.combo_id
			   JOIN product_variants pv ON ci.variant_id = pv.id
			   JOIN orders o ON oi.order_id = o.id
			   WHERE pv.product_id = :productId AND o.status IN (:statuses))
			""", nativeQuery = true)
	Integer sumQuantityByProductId(@Param("productId") Long productId, @Param("statuses") List<String> statuses);

	@Query("""
			SELECT p.name, SUM(oi.quantity) as totalSold
			FROM OrderItem oi
			LEFT JOIN oi.variant v
			LEFT JOIN v.product p
			WHERE oi.order.status IN :statuses
			GROUP BY p.name
			ORDER BY totalSold DESC
			""")
	List<Object[]> getTopSellingProducts(@Param("statuses") List<OrderStatus> statuses, Pageable pageable);
}
