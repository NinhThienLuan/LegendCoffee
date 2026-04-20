package fpt.legendcoffee.controller;

import fpt.legendcoffee.dto.LoginRequestDTO;
import fpt.legendcoffee.dto.RegisterRequestDTO;
import fpt.legendcoffee.service.AuthenService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextHolderStrategy;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final SecurityContextRepository securityContextRepository;
    private final SecurityContextHolderStrategy securityContextHolderStrategy = SecurityContextHolder.getContextHolderStrategy();
    private final AuthenService authenService;
    
    @GetMapping("/home")
    public String home() {
        return "index";
    }

    @GetMapping("/login")
    public String showLoginForm(HttpSession session) {
        // Clear session messages after they are accessed by the view
        // In a real app, you might use RedirectAttributes for this
        return "authen/login";
    }

    @PostMapping("/login")
    public String login(@Valid @ModelAttribute("loginDto") LoginRequestDTO request,
                        HttpServletRequest httpRequest,
                        HttpServletResponse httpResponse,
                        RedirectAttributes redirectAttributes) {
        try {
            Authentication authenticationRequest =
                    UsernamePasswordAuthenticationToken.unauthenticated(request.email(), request.password());
            Authentication authenticationResponse =
                    this.authenticationManager.authenticate(authenticationRequest);

            SecurityContext context = this.securityContextHolderStrategy.createEmptyContext();
            context.setAuthentication(authenticationResponse);
            this.securityContextHolderStrategy.setContext(context);
            this.securityContextRepository.saveContext(context, httpRequest, httpResponse);

            return "redirect:/home";
        } catch (Exception e) {
            redirectAttributes.addAttribute("error", e.getMessage());
            return "redirect:/login";
        }
    }

    @GetMapping("/register")
    public String showRegisterForm(HttpSession session) {
        return "authen/register";
    }

    @PostMapping("/register")
    public String register(@Valid @ModelAttribute("registerDto") RegisterRequestDTO request, 
                           RedirectAttributes redirectAttributes) {
        try {
            authenService.register(request);
            redirectAttributes.addAttribute("success", "Đăng ký thành công! Vui lòng đăng nhập.");
            return "redirect:/login";
        } catch (Exception e) {
            redirectAttributes.addAttribute("error", e.getMessage());
            return "redirect:/register";
        }
    }
}
