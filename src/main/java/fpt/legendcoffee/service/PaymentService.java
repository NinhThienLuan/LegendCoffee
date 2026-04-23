package fpt.legendcoffee.service;

import fpt.legendcoffee.entity.Payment;
import fpt.legendcoffee.entity.enumeration.PaymentStatus;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public interface PaymentService {
    Optional<Payment> findByOrderIdAndStatus(Long orderId, PaymentStatus paymentStatus);
}
