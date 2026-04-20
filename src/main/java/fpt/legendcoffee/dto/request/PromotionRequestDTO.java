package fpt.legendcoffee.dto.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PromotionRequestDTO {

    @NotBlank(message = "Type không được để trống")
    @Pattern(regexp = "PERCENT|FIXED", message = "Type phải là PERCENT hoặc FIXED")
    private String type;

    @NotNull(message = "Value không được null")
    @DecimalMin(value = "0.0", inclusive = false, message = "Value phải > 0")
    private BigDecimal value;

    @NotNull(message = "Start date không được null")
    private LocalDateTime startDate;

    @NotNull(message = "End date không được null")
    private LocalDateTime endDate;
}
