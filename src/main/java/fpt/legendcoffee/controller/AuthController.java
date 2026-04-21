package fpt.legendcoffee.controller;

import fpt.legendcoffee.dto.request.ForgotPasswordRequestDTO;
import fpt.legendcoffee.dto.request.LoginRequestDTO;
import fpt.legendcoffee.dto.request.RegisterRequestDTO;
import fpt.legendcoffee.dto.request.ResetPasswordRequestDTO;
import fpt.legendcoffee.service.AuthenService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContext;
import fpt.legendcoffee.common.security.CustomUserDetails;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextHolderStrategy;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Slf4j
@Controller
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final SecurityContextRepository securityContextRepository;
    private final SecurityContextHolderStrategy securityContextHolderStrategy = SecurityContextHolder
            .getContextHolderStrategy();
    private final AuthenService authenService;

    @GetMapping("/home")
    public String home() {
        return "index";
    }

    @GetMapping("/login")
    public String showLoginForm(Model model) {
        if (isAuthenticated()) {
            return "redirect:/home";
        }
        if (!model.containsAttribute("loginDto")) {
            model.addAttribute("loginDto", new LoginRequestDTO("", ""));
        }
        return "authen/login";
    }

    @PostMapping("/login")
    public String login(@Valid @ModelAttribute("loginDto") LoginRequestDTO request,
            BindingResult bindingResult,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse,
            Model model) {
        if (bindingResult.hasErrors()) {
            String message = bindingResult.getFieldError() != null
                    ? bindingResult.getFieldError().getDefaultMessage()
                    : "Thông tin đăng nhập không hợp lệ.";
            model.addAttribute("errorMessage", message);
            return "authen/login";
        }

        try {
            Authentication authenticationRequest = UsernamePasswordAuthenticationToken.unauthenticated(request.email(),
                    request.password());
            Authentication authenticationResponse = this.authenticationManager.authenticate(authenticationRequest);

            SecurityContext context = this.securityContextHolderStrategy.createEmptyContext();
            context.setAuthentication(authenticationResponse);
            this.securityContextHolderStrategy.setContext(context);
            this.securityContextRepository.saveContext(context, httpRequest, httpResponse);

            return "redirect:/home";
        } catch (Exception e) {
            log.warn("Login failed for email={}", request.email());
            model.addAttribute("errorMessage", "Email hoặc mật khẩu không chính xác.");
            return "authen/login";
        }
    }

    @GetMapping("/register")
    public String showRegisterForm(Model model) {
        if (isAuthenticated()) {
            return "redirect:/home";
        }
        if (!model.containsAttribute("registerDto")) {
            model.addAttribute("registerDto", new RegisterRequestDTO("", "", "", "", ""));
        }
        return "authen/register";
    }

    @PostMapping("/register")
    public String register(@Valid @ModelAttribute("registerDto") RegisterRequestDTO request,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            String message = bindingResult.getFieldError() != null
                    ? bindingResult.getFieldError().getDefaultMessage()
                    : "Dữ liệu đăng ký không hợp lệ.";
            model.addAttribute("errorMessage", message);
            return "authen/register";
        }

        try {
            authenService.register(request);
            redirectAttributes.addFlashAttribute("successMessage", "Đăng ký thành công! Vui lòng đăng nhập.");
            return "redirect:/login";
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "authen/register";
        }
    }

    @GetMapping("/forgot-password")
    public String showForgotPasswordForm() {
        if (isAuthenticated()) {
            return "redirect:/home";
        }
        return "authen/change-password"; // Using change-password as recovery template
    }

    @PostMapping("/forgot-password")
    public String forgotPassword(@Valid @ModelAttribute("forgotPasswordDto") ForgotPasswordRequestDTO request,
            RedirectAttributes redirectAttributes) {
        try {
            authenService.forgotPassword(request.email());
            redirectAttributes.addAttribute("success", "Mật khẩu mới đã được gửi vào email của bạn.");
            return "redirect:/login";
        } catch (Exception e) {
            redirectAttributes.addAttribute("error", e.getMessage());
            return "redirect:/login";
        }
    }

    @GetMapping("/reset-password")
    public String showResetPasswordForm() {
        if (isAuthenticated()) {
            return "redirect:/home";
        }
        return "authen/change-password";
    }

    @PostMapping("/reset-password")
    public String resetPassword(@Valid @ModelAttribute("resetPasswordDto") ResetPasswordRequestDTO request,
            RedirectAttributes redirectAttributes) {
        try {
            if (!request.newPassword().equals(request.confirmPassword())) {
                throw new Exception("Mật khẩu xác nhận không khớp.");
            }
            authenService.resetPassword(request.email(), request.oldPassword(), request.newPassword());
            redirectAttributes.addAttribute("success", "Đổi mật khẩu thành công! Vui lòng đăng nhập lại.");
            return "redirect:/login";
        } catch (Exception e) {
            redirectAttributes.addAttribute("error", e.getMessage());
            return "redirect:/change-password";
        }
    }

    @GetMapping("/profile")
    public String showProfileForm(Model model, @AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails == null) {
            return "redirect:/login";
        }

        long userId = userDetails.getUser().getId();
        model.addAttribute("profileDto", authenService.getProfile(userId));
        return "authen/profile";
    }
    private boolean isAuthenticated() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getPrincipal().equals("anonymousUser")) {
            return false;
        }
        return authentication.isAuthenticated();
    }
}
