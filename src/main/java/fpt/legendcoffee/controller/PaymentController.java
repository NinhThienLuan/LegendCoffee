package fpt.legendcoffee.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PaymentController {

    @GetMapping("/payment")
    public String paymentPage(Model model) {
        // TODO: Add payment details from order

        model.addAttribute("orderId", 1);
        model.addAttribute("totalAmount", 22880000);
        return "payment/payment";
    }
}
