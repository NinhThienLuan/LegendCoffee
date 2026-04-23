package fpt.legendcoffee.job;

import fpt.legendcoffee.entity.Payment;
import fpt.legendcoffee.entity.enumeration.PaymentStatus;
import fpt.legendcoffee.repository.PaymentRepository;
import fpt.legendcoffee.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentJob {

    private final PaymentRepository paymentRepository;
    private final OrderService orderService;

    private static final int MAX_EXP = 15;
    private static final int CHECK_INTERVAL = 5;
    /**
     * Clear expired payments (set status to FAILED) that have been PENDING for more than 15 minutes.
     * Runs every 5 minutes.
     */
    @Scheduled(cron = "0 */" + CHECK_INTERVAL + " * * * *")
    @Transactional
    public void clearExpiredPayments() {
        log.info("Starting background job: clearExpiredPayments");
        
        LocalDateTime expirationThreshold = LocalDateTime.now().minusMinutes(MAX_EXP);
        
        List<Payment> expiredPayments = paymentRepository.findByStatusAndCreatedAtBefore(
                PaymentStatus.PENDING, 
                expirationThreshold
        );

        if (!expiredPayments.isEmpty()) {
            log.info("Found {} expired payments to clear", expiredPayments.size());
            for (Payment payment : expiredPayments) {
                payment.setStatus(PaymentStatus.FAILED);
                payment.setErrorMessage("Payment expired (automatically cleared by background job)");
                
                // Cancel the order and restore stock
                if (payment.getOrder() != null) {
                    try {
                        orderService.cancelOrder(payment.getOrder().getId());
                    } catch (Exception e) {
                        log.error("Error cancelling order #{} during payment clearing: {}", 
                                payment.getOrder().getId(), e.getMessage());
                    }
                }
            }
            paymentRepository.saveAll(expiredPayments);
            log.info("Successfully cleared {} expired payments and cancelled associated orders", expiredPayments.size());
        } else {
            log.info("No expired payments found.");
        }
    }
}
