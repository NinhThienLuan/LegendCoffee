package fpt.legendcoffee.entity.enumeration;

public enum WithdrawalStatus {
    PENDING,   // Đang chờ admin xét duyệt
    APPROVED,  // Admin đã chấp nhận — tiền đã chuyển ra ngoài
    REJECTED   // Admin từ chối — tiền đã hoàn lại vào available
}
