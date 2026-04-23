package fpt.legendcoffee.dto.response;

import lombok.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardResponseDTO {
    private BigDecimal totalRevenue;
    private BigDecimal totalShippingFees;
    private BigDecimal totalWithdrawals;
    private long newUsers;

    private List<String> revenueLabels;
    private List<BigDecimal> revenueData;

    private List<String> topProductLabels;
    private List<Long> topProductData;

    private Map<String, Long> orderStatusDistribution;
}
