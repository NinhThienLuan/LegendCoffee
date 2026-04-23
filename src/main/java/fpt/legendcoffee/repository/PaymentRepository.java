package fpt.legendcoffee.repository;

import fpt.legendcoffee.entity.Payment;
import fpt.legendcoffee.entity.enumeration.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Optional<Payment> findByTransRef(String transRef);

    Optional<Payment> findByOrderIdAndStatus(Long orderId, fpt.legendcoffee.entity.enumeration.PaymentStatus status);


    List<Payment> findByStatusAndCreatedAtBefore(PaymentStatus paymentStatus, LocalDateTime expirationThreshold);
}
