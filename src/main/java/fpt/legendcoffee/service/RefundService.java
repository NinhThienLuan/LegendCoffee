package fpt.legendcoffee.service;

import java.math.BigDecimal;

import fpt.legendcoffee.entity.RefundRequest;

public interface RefundService {
    
    RefundRequest createRefundRequest(Long userId, Long orderId, BigDecimal amount, String reason);

    void approveRefund(Long refundRequestId);

    void rejectRefund(Long refundRequestId);
    
}