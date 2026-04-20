package fpt.legendcoffee.dto.response;

import fpt.legendcoffee.entity.enumeration.VoucherType;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class VoucherResponseDTO {
    private Long id;
    private String code;
    private VoucherType type;
    private BigDecimal value;
    private BigDecimal conditionMin;
    private Integer usageLimit;
    private Integer usedCount;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private boolean active;

}
