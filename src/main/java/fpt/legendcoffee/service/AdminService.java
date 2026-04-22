package fpt.legendcoffee.service;

public interface AdminService {
    /**
     * Phê duyệt yêu cầu rút tiền.
     * @param transactionId ID của giao dịch cần duyệt.
     */
    void approveWithdrawal(Long transactionId);

    /**
     * Từ chối yêu cầu rút tiền và hoàn tiền cho người dùng.
     * @param transactionId ID của giao dịch cần từ chối.
     * @param note Lý do từ chối.
     */
    void rejectWithdrawal(Long transactionId, String note);

    /**
     * Lấy danh sách các yêu cầu rút tiền đang chờ duyệt.
     */
    java.util.List<fpt.legendcoffee.entity.WalletTransaction> getPendingWithdrawals();
}
