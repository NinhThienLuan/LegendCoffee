package fpt.legendcoffee.repository;

import fpt.legendcoffee.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Optional<Payment> findByTransRef(String transRef);

    Optional<Payment> findByOrderIdAndStatus(Long orderId, fpt.legendcoffee.entity.enumeration.PaymentStatus status);
}
