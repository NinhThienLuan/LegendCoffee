package fpt.legendcoffee.service.serviceImpl;

import fpt.legendcoffee.entity.Payment;
import fpt.legendcoffee.entity.enumeration.PaymentStatus;
import fpt.legendcoffee.repository.PaymentRepository;
import fpt.legendcoffee.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class PaymentServiceImpl implements PaymentService {

    @Autowired
    private PaymentRepository paymentRepository;

    @Override
    public Optional<Payment> findByOrderIdAndStatus(Long orderId, PaymentStatus paymentStatus) {
        return paymentRepository.findByOrderIdAndStatus(orderId, paymentStatus);
    }
}
