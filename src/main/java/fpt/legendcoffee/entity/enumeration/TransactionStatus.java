package fpt.legendcoffee.entity.enumeration;

/**
 * Trạng thái của một giao dịch tài chính.
 */
public enum TransactionStatus {
    PENDING, // Đang chờ duyệt
    SUCCESS, // Thành công
    FAILED   // Thất bại / Bị từ chối
}
