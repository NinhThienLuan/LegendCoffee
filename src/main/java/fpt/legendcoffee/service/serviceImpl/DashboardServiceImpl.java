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

    @Override
    public DashboardResponseDTO getDashboardStats() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startOfMonth = now.withDayOfMonth(1).with(LocalTime.MIN);
        LocalDateTime startOfLastMonth = now.minusMonths(1).withDayOfMonth(1).with(LocalTime.MIN);
        LocalDateTime startOfToday = now.with(LocalTime.MIN);
        LocalDateTime sixMonthsAgo = now.minusMonths(6).withDayOfMonth(1).with(LocalTime.MIN);

        List<OrderStatus> revenueStatuses = Arrays.asList(OrderStatus.CONFIRMED, OrderStatus.SHIPPING, OrderStatus.COMPLETED);

        // KPIs
        BigDecimal monthlyRevenue = orderRepository.sumRevenueSince(startOfMonth, revenueStatuses);
        BigDecimal lastMonthRevenue = orderRepository.sumRevenueBetween(startOfLastMonth, startOfMonth, revenueStatuses);

        if (monthlyRevenue == null) monthlyRevenue = BigDecimal.ZERO;
        if (lastMonthRevenue == null) lastMonthRevenue = BigDecimal.ZERO;

        double growth = 0;
        if (lastMonthRevenue.compareTo(BigDecimal.ZERO) > 0) {
            growth = (monthlyRevenue.subtract(lastMonthRevenue))
                    .divide(lastMonthRevenue, 4, java.math.RoundingMode.HALF_UP)
                    .doubleValue() * 100;
        }

        long todayOrders = orderRepository.countOrdersSince(startOfToday, revenueStatuses);
        long activeProducts = productService.countActiveProducts();
        long lowStockProducts = productService.countLowStockProducts();
        long newUsers = userRepository.countUsersSince(startOfMonth);

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
                .monthlyRevenue(monthlyRevenue)
                .revenueGrowth(growth)
                .todayOrders(todayOrders)
                .activeProducts(activeProducts)
                .lowStockProducts(lowStockProducts)
                .newUsers(newUsers)
                .orderStatusDistribution(statusMap)
                .topProductLabels(topLabels)
                .topProductData(topData)
                .revenueLabels(revLabels)
                .revenueData(revData)
                .build();
    }
}
