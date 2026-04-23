package fpt.legendcoffee.service.serviceImpl;

import fpt.legendcoffee.entity.OrderItem;
import fpt.legendcoffee.repository.OrderItemRepository;
import fpt.legendcoffee.service.OrderItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OrderItemServiceImpl implements OrderItemService {

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Override
    public List<OrderItem> findByOrderIdWithDetails(Long orderId) {
        return orderItemRepository.findByOrderIdWithDetails(orderId);
    }
}
