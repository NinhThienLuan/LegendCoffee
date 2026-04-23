package fpt.legendcoffee.dto.app;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WithdrawalRequestDTO {

    @NotNull(message = "Số tiền không được để trống")
    @DecimalMin(value = "10000", message = "Số tiền rút tối thiểu là 10.000 ₫")
    private BigDecimal amount;

    @NotBlank(message = "Vui lòng nhập tên ngân hàng")
    private String bankName;

    @NotBlank(message = "Vui lòng nhập mã ngân hàng")
    private String bankCode;

    @NotBlank(message = "Vui lòng nhập số tài khoản")
    private String accountNumber;

    @NotBlank(message = "Vui lòng nhập tên chủ tài khoản")
    private String accountHolder;
}
