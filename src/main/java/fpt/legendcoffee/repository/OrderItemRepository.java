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
	@Query("""
			SELECT SUM(oi.quantity)
			FROM OrderItem oi
			LEFT JOIN oi.variant v
			WHERE v.product.id = :productId
			AND oi.order.status NOT IN (fpt.legendcoffee.entity.enumeration.OrderStatus.CANCELLED)
			""")
	Integer sumQuantityByProductId(@Param("productId") Long productId);

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

