package fpt.legendcoffee.service;

import fpt.legendcoffee.dto.app.CheckoutRequestDTO;
import fpt.legendcoffee.dto.app.OrderListDTO;
import fpt.legendcoffee.entity.Order;
import fpt.legendcoffee.entity.enumeration.OrderStatus;

import java.util.List;

public interface OrderService {
    Order createOrder(CheckoutRequestDTO checkout);

    List<OrderListDTO> getAllOrdersForList();
    List<OrderListDTO> getOrdersByStatus(OrderStatus status);
    void startDelivering(Long orderId);
    Order getOrderWithDetails(Long id);
    void cancelOrder(Long orderId);
    void completeDelivery(Long orderId);

    int getMaxQuantityPerItem();
    int getMaxTotalQuantity();

}

