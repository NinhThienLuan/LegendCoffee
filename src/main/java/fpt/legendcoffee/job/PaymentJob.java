package fpt.legendcoffee.job;

import fpt.legendcoffee.entity.Payment;
import fpt.legendcoffee.entity.enumeration.PaymentStatus;
import fpt.legendcoffee.repository.PaymentRepository;
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

    /**
     * Clear expired payments (set status to FAILED) that have been PENDING for more than 15 minutes.
     * Runs every 5 minutes.
     */
    @Scheduled(cron = "0 */5 * * * *")
    @Transactional
    public void clearExpiredPayments() {
        log.info("Starting background job: clearExpiredPayments");
        
        LocalDateTime expirationThreshold = LocalDateTime.now().minusMinutes(15);
        
        List<Payment> expiredPayments = paymentRepository.findByStatusAndCreatedAtBefore(
                PaymentStatus.PENDING, 
                expirationThreshold
        );

        if (!expiredPayments.isEmpty()) {
            log.info("Found {} expired payments to clear", expiredPayments.size());
            for (Payment payment : expiredPayments) {
                payment.setStatus(PaymentStatus.FAILED);
                payment.setErrorMessage("Payment expired (automatically cleared by background job)");
            }
            paymentRepository.saveAll(expiredPayments);
            log.info("Successfully cleared {} expired payments", expiredPayments.size());
        } else {
            log.info("No expired payments found.");
        }
    }
}
