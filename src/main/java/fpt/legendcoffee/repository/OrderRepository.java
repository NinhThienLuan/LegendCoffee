package fpt.legendcoffee.repository;

import fpt.legendcoffee.entity.Order;
import fpt.legendcoffee.entity.User;
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
}
