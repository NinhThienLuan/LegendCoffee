package fpt.legendcoffee.service;

import fpt.legendcoffee.entity.OrderItem;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface OrderItemService {
    List<OrderItem> findByOrderIdWithDetails(Long orderId);
}
