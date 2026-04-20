package fpt.legendcoffee.service;

import fpt.legendcoffee.dto.app.CheckoutRequestDTO;
import fpt.legendcoffee.entity.Order;

public interface OrderService {
    Order createOrder(CheckoutRequestDTO checkout);
}
