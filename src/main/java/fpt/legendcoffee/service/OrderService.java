package fpt.legendcoffee.service;

import fpt.legendcoffee.dto.app.CheckoutRequestDTO;
import fpt.legendcoffee.dto.app.OrderListDTO;
import fpt.legendcoffee.entity.Order;

import java.util.List;

public interface OrderService {
    Order createOrder(CheckoutRequestDTO checkout);

    List<OrderListDTO> getAllOrdersForList();
    void startDelivering(Long orderId);

}
