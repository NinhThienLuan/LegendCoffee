package fpt.legendcoffee.dto.request;

import java.math.BigDecimal;

/**
 * DTO truyền kết quả thanh toán vào Thymeleaf view (trang payment.html).
 * Được build từ query params của VNPay Return URL — không cần truy vấn DB.
 */
public class PaymentReturnDTO {

    private boolean success;
    private String message;

    private String txnRef; // vnp_TxnRef — mã đơn hàng
    private BigDecimal amount; // vnp_Amount / 100 — số tiền thực (VND)
    private String transactionNo; // vnp_TransactionNo — mã giao dịch VNPay
    private String bankCode; // vnp_BankCode — ngân hàng thanh toán
    private String responseCode; // vnp_ResponseCode — "00" = thành công
    private String payDate; // vnp_PayDate — thời gian thanh toán

    public PaymentReturnDTO() {
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getTxnRef() {
        return txnRef;
    }

    public void setTxnRef(String txnRef) {
        this.txnRef = txnRef;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getTransactionNo() {
        return transactionNo;
    }

    public void setTransactionNo(String transactionNo) {
        this.transactionNo = transactionNo;
    }

    public String getBankCode() {
        return bankCode;
    }

    public void setBankCode(String bankCode) {
        this.bankCode = bankCode;
    }

    public String getResponseCode() {
        return responseCode;
    }

    public void setResponseCode(String responseCode) {
        this.responseCode = responseCode;
    }

    public String getPayDate() {
        return payDate;
    }

    public void setPayDate(String payDate) {
        this.payDate = payDate;
    }

    public static PaymentReturnDTO success(String txnRef, BigDecimal amount,
            String transactionNo, String bankCode,
            String responseCode, String payDate) {
        PaymentReturnDTO dto = new PaymentReturnDTO();
        dto.success = true;
        dto.message = "Thanh toán thành công";
        dto.txnRef = txnRef;
        dto.amount = amount;
        dto.transactionNo = transactionNo;
        dto.bankCode = bankCode;
        dto.responseCode = responseCode;
        dto.payDate = payDate;
        return dto;
    }

    public static PaymentReturnDTO failure(String message, String responseCode, String txnRef) {
        PaymentReturnDTO dto = new PaymentReturnDTO();
        dto.success = false;
        dto.message = message;
        dto.responseCode = responseCode;
        dto.txnRef = txnRef;
        return dto;
    }
}
