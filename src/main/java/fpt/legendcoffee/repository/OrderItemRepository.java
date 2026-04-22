package fpt.legendcoffee.repository;

import fpt.legendcoffee.entity.OrderItem;
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
			WHERE oi.order.id = :orderId
			""")
	List<OrderItem> findByOrderIdWithDetails(@Param("orderId") Long orderId);
}

