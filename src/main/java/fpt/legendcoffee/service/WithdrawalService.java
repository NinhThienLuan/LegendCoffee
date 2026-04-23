package fpt.legendcoffee.service;

import fpt.legendcoffee.dto.app.WithdrawalRequestDTO;
import fpt.legendcoffee.entity.WithdrawalRequest;

import java.util.List;

public interface WithdrawalService {

    /**
     * User tạo yêu cầu rút tiền.
     * Reserved pattern: availableAmount -= amount, reservedAmount += amount.
     */
    WithdrawalRequest requestWithdrawal(Long userId, WithdrawalRequestDTO dto);

    /**
     * Admin chấp nhận yêu cầu rút tiền.
     * reservedAmount -= amount (tiền rời hệ thống).
     */
    WithdrawalRequest approveWithdrawal(Long requestId);

    /**
     * Admin từ chối yêu cầu rút tiền kèm lý do.
     * reservedAmount -= amount, availableAmount += amount (hoàn lại).
     */
    WithdrawalRequest rejectWithdrawal(Long requestId, String reason);

    /** Lấy danh sách yêu cầu của một user cụ thể */
    List<WithdrawalRequest> getRequestsByUser(Long userId);

    /** Admin: lấy tất cả yêu cầu (mới nhất trước) */
    List<WithdrawalRequest> getAllRequests();
}
