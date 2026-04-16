package fpt.legendcoffee.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class OrderController {

//    @Autowired
//    OrderRepository orderRepo;
//
//    @GetMapping("/order-detail/{id}")
//    public Order orderDetail(@PathVariable long id) {
//        return orderRepo.findById(id).orElse(null);
//    }

//    @GetMapping("/orders")
//    public String orderList(Model model) {
//        model.addAttribute("orders", orderRepo.findAll());
//        return "order/order";  // Maps to: templates/order/order.html
//    }

//    @GetMapping("/orders/{id}")
//    public ResponseEntity<Order> getOrder(@PathVariable long id) {
//        return orderRepo.findById(id)
//                .map(ResponseEntity::ok)
//                .orElse(ResponseEntity.notFound().build());
//    }

    @GetMapping("/order")
    public String orderPage(Model model) {
        return "order/order";
    }

    @GetMapping("/order-detail")
    public String orderDetailPage(Model model) {
        return "order-detail/orderDetail";
    }

    @GetMapping("/cart")
    public String cartPage(Model model) {
        return "cart/cart";
    }

    @GetMapping("/checkout")
    public String checkoutPage(Model model) {
        return "checkout/checkout";
    }
}
