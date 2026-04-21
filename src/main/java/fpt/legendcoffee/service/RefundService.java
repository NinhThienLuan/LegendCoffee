package fpt.legendcoffee.service;

import fpt.legendcoffee.entity.RefundRequest;

import java.math.BigDecimal;

public interface RefundService {
    
    RefundRequest createRefundRequest(Long userId, Long orderId, BigDecimal amount, String reason);

    void approveRefund(Long refundRequestId);

    void rejectRefund(Long refundRequestId);
    
}