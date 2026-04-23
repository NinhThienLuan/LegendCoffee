package fpt.legendcoffee.service.serviceImpl;

import fpt.legendcoffee.dto.response.DashboardResponseDTO;
import fpt.legendcoffee.repository.OrderItemRepository;
import fpt.legendcoffee.repository.OrderRepository;
import fpt.legendcoffee.repository.UserRepository;
import fpt.legendcoffee.entity.enumeration.OrderStatus;
import fpt.legendcoffee.service.DashboardService;
import fpt.legendcoffee.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final OrderItemRepository orderItemRepository;
    private final ProductService productService;
    private final fpt.legendcoffee.repository.WithdrawalRequestRepository withdrawalRequestRepository;

    @Override
    public DashboardResponseDTO getDashboardStats() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startOfMonth = now.withDayOfMonth(1).with(LocalTime.MIN);
        LocalDateTime sixMonthsAgo = now.minusMonths(6).withDayOfMonth(1).with(LocalTime.MIN);
        List<OrderStatus> revenueStatuses = Arrays.asList(OrderStatus.CONFIRMED, OrderStatus.SHIPPING, OrderStatus.COMPLETED);

        long newUsers = userRepository.countUsersSince(startOfMonth);

        // System-wide Financials
        BigDecimal totalRevenue = orderRepository.sumTotalRevenue(revenueStatuses);
        BigDecimal totalShippingFees = orderRepository.sumTotalShippingFee(revenueStatuses);
        BigDecimal totalWithdrawals = withdrawalRequestRepository.sumTotalWithdrawals(fpt.legendcoffee.entity.enumeration.WithdrawalStatus.APPROVED);

        if (totalRevenue == null) totalRevenue = BigDecimal.ZERO;
        if (totalShippingFees == null) totalShippingFees = BigDecimal.ZERO;
        if (totalWithdrawals == null) totalWithdrawals = BigDecimal.ZERO;

        // Order Status distribution
        List<Object[]> statusCounts = orderRepository.countOrdersByStatus();
        Map<String, Long> rawStatusMap = statusCounts.stream()
                .collect(Collectors.toMap(
                        obj -> obj[0].toString(),
                        obj -> (Long) obj[1]
                ));

        Map<String, Long> statusMap = new HashMap<>();
        statusMap.put("COMPLETED", rawStatusMap.getOrDefault("COMPLETED", 0L));
        statusMap.put("SHIPPING", rawStatusMap.getOrDefault("SHIPPING", 0L));
        statusMap.put("PENDING", rawStatusMap.getOrDefault("PENDING", 0L) + rawStatusMap.getOrDefault("CONFIRMED", 0L));
        statusMap.put("CANCELLED", rawStatusMap.getOrDefault("CANCELLED", 0L));

        // Top selling products
        List<Object[]> topProductList = orderItemRepository.getTopSellingProducts(revenueStatuses, PageRequest.of(0, 5));
        List<String> topLabels = topProductList.stream().map(obj -> (String) obj[0]).collect(Collectors.toList());
        List<Long> topData = topProductList.stream().map(obj -> (Long) obj[1]).collect(Collectors.toList());

        // Monthly revenue chart
        List<Object[]> monthlyRevenueList = orderRepository.getMonthlyRevenueSince(sixMonthsAgo, revenueStatuses);
        List<String> revLabels = new ArrayList<>();
        List<BigDecimal> revData = new ArrayList<>();
        
        // Populate labels for last 6 months to ensure consistency even if some months have 0 revenue
        for (int i = 5; i >= 0; i--) {
            LocalDateTime monthDate = now.minusMonths(i);
            int monthValue = monthDate.getMonthValue();
            String label = "Tháng " + monthValue;
            revLabels.add(label);
            
            BigDecimal revenue = monthlyRevenueList.stream()
                    .filter(obj -> ((Integer) obj[0]) == monthValue)
                    .map(obj -> (BigDecimal) obj[1])
                    .findFirst()
                    .orElse(BigDecimal.ZERO);
            revData.add(revenue.divide(new BigDecimal(1000000), 1, java.math.RoundingMode.HALF_UP)); // Convert to millions for display
        }

        return DashboardResponseDTO.builder()
                .totalRevenue(totalRevenue)
                .totalShippingFees(totalShippingFees)
                .totalWithdrawals(totalWithdrawals)
                .newUsers(newUsers)
                .orderStatusDistribution(statusMap)
                .topProductLabels(topLabels)
                .topProductData(topData)
                .revenueLabels(revLabels)
                .revenueData(revData)
                .build();
    }
}
