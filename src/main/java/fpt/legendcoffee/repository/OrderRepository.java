package fpt.legendcoffee.repository;

import fpt.legendcoffee.entity.Order;
import fpt.legendcoffee.entity.User;
import fpt.legendcoffee.entity.enumeration.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
	List<Order> findByUserOrderByOrderDateDesc(User user);

	@Query("SELECT o FROM Order o LEFT JOIN FETCH o.user WHERE o.id = :id")
	Optional<Order> findByIdWithUser(@Param("id") Long id);

	@Query("SELECT o FROM Order o LEFT JOIN FETCH o.user ORDER BY o.orderDate DESC")
	List<Order> findAllWithUser();

	@Query("SELECT o FROM Order o LEFT JOIN FETCH o.user WHERE o.status = :status ORDER BY o.orderDate DESC")
	List<Order> findByStatusWithUser(@Param("status") OrderStatus status);

	@Query("SELECT DISTINCT o FROM Order o " +
			"LEFT JOIN FETCH o.orderItems oi " +
			"LEFT JOIN FETCH oi.variant v " +
			"LEFT JOIN FETCH v.product p " +
			"LEFT JOIN FETCH o.shippingInfo s " +
			"WHERE o.status = :status " +
			"ORDER BY o.orderDate DESC")
	List<Order> findByStatusWithDetails(@Param("status") OrderStatus status);
	@Query("SELECT DISTINCT o FROM Order o " +
			"LEFT JOIN FETCH o.orderItems oi " +
			"LEFT JOIN FETCH oi.variant v " +
			"LEFT JOIN FETCH v.product p " +
			"LEFT JOIN FETCH o.shippingInfo s " +
			"ORDER BY o.orderDate DESC")
	List<Order> findAllWithDetails();

	@Query("SELECT DISTINCT o FROM Order o " +
			"LEFT JOIN FETCH o.orderItems oi " +
			"LEFT JOIN FETCH oi.variant v " +
			"LEFT JOIN FETCH v.product p " +
			"LEFT JOIN FETCH o.shippingInfo s " +
			"WHERE o.id = :id")
	Optional<Order> findByIdWithDetails(@Param("id") Long id);

	@Query("SELECT o.status, COUNT(o) FROM Order o GROUP BY o.status")
	List<Object[]> countOrdersByStatus();

	@Query("SELECT SUM(o.totalAmount) FROM Order o WHERE o.status IN :statuses AND o.orderDate >= :startDate AND o.orderDate < :endDate")
	java.math.BigDecimal sumRevenueBetween(@Param("startDate") java.time.LocalDateTime startDate, @Param("endDate") java.time.LocalDateTime endDate, @Param("statuses") List<OrderStatus> statuses);

	@Query("SELECT SUM(o.totalAmount) FROM Order o WHERE o.status IN :statuses AND o.orderDate >= :startDate")
	java.math.BigDecimal sumRevenueSince(@Param("startDate") java.time.LocalDateTime startDate, @Param("statuses") List<OrderStatus> statuses);

	@Query("SELECT COUNT(o) FROM Order o WHERE o.status IN :statuses AND o.orderDate >= :startDate")
	long countOrdersSince(@Param("startDate") java.time.LocalDateTime startDate, @Param("statuses") List<OrderStatus> statuses);

	@Query("SELECT MONTH(o.orderDate), SUM(o.totalAmount) FROM Order o " +
			"WHERE o.status IN :statuses " +
			"AND o.orderDate >= :startDate " +
			"GROUP BY MONTH(o.orderDate) " +
			"ORDER BY MONTH(o.orderDate)")
	List<Object[]> getMonthlyRevenueSince(@Param("startDate") java.time.LocalDateTime startDate, @Param("statuses") List<OrderStatus> statuses);
}
