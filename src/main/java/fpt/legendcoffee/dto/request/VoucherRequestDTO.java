//package fpt.legendcoffee.dto.request;
//
//import fpt.legendcoffee.entity.enumeration.VoucherType;
//import jakarta.validation.constraints.*;
//import lombok.*;
//
//import java.math.BigDecimal;
//import java.time.LocalDateTime;
//
//@Getter
//@Setter
//@AllArgsConstructor
//@NoArgsConstructor
//public class VoucherRequestDTO {
//
//    @NotBlank(message = "Code không được để trống")
//    private String code;
//
//    @NotNull(message = "Type không được null")
//    private VoucherType type;
//
//    @NotNull(message = "Value không được null")
//    @DecimalMin(value = "0.0", inclusive = false, message = "Value phải > 0")
//    private BigDecimal value;
//
//    @NotNull(message = "Start date không được null")
//    private LocalDateTime startDate;
//
//    @NotNull(message = "End date không được null")
//    private LocalDateTime endDate;
//
//    @NotNull(message = "Min condition không được null")
//    @DecimalMin(value = "0.0", message = "Min condition phải >= 0")
//    private BigDecimal conditionMin;
//
//    @NotNull(message = "Usage limit không được null")
//    @Min(value = 1, message = "Usage limit phải >= 1")
//    private Integer usageLimit;
//
//    public void validate() {
//        if (endDate.isBefore(startDate)) {
//            throw new IllegalArgumentException("End date phải sau start date");
//        }
//    }
//}
