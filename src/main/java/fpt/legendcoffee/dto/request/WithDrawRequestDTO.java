package fpt.legendcoffee.dto.request;

import java.math.BigDecimal;

import fpt.legendcoffee.entity.enumeration.BankCode;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class WithDrawRequestDTO {

    @NotNull(message = "Số tiền rút không được để trống")
    @DecimalMin(value = "50000", message = "Số tiền rút tối thiểu là 50,000 VNĐ")
    private BigDecimal amount;

    @NotNull(message = "Vui lòng chọn ngân hàng")
    private BankCode bankCode; 

    @NotBlank(message = "Số tài khoản không được bỏ trống")
    private String accountNumber;

    @NotBlank(message = "Tên chủ tài khoản không được bỏ trống")
    private String accountName;
}