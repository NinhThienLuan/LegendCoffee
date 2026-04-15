package fpt.legendcoffee.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AuthController {

    @GetMapping("/home")
    public String home() {
        return "index";
    }

    @GetMapping("/login")
    public String showLoginForm() {
        return "authen/login/login";
    }

    @PostMapping("/login")
    public String login(@RequestParam String username, @RequestParam String password) {
        // TODO: Implement login logic (authentication service)
        // For now, redirect to home on success
        return "redirect:/home";
    }

    @GetMapping("/register")
    public String showRegisterForm() {
        return "authen/register/register";
    }

    @PostMapping("/register")
    public String register(@RequestParam String username, @RequestParam String password) {
        // TODO: Implement registration logic (user service)
        // For now, redirect to login on success
        return "redirect:/login";
    }
}
