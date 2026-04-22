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
        if (!model.containsAttribute("forgotPasswordDto")) {
            model.addAttribute("forgotPasswordDto", new ForgotPasswordRequestDTO(""));
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

            String forceChangeEmail = (String) httpRequest.getSession().getAttribute("forceChangeEmail");
            if (request.email().equalsIgnoreCase(forceChangeEmail)) {
                return "redirect:/login?forceChange=true&email=" + request.email();
            }

            CustomUserDetails userDetails = (CustomUserDetails) authenticationResponse.getPrincipal();
            boolean isAdmin = userDetails.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
            return isAdmin ? "redirect:/admin" : "redirect:/home";
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
    public String showForgotPasswordForm(Model model) {
        if (!model.containsAttribute("forgotPasswordDto")) {
            model.addAttribute("forgotPasswordDto", new ForgotPasswordRequestDTO(""));
        }
        return "authen/login";
    }

    @PostMapping("/forgot-password")
    public String forgotPassword(@Valid @ModelAttribute("forgotPasswordDto") ForgotPasswordRequestDTO request,
                                 BindingResult bindingResult,
                                 RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Email không hợp lệ.");
            return "redirect:/login";
        }
        try {
            authenService.forgotPassword(request.email());
            log.info("Forgot password email sent successfully to: {}", request.email());
            redirectAttributes.addFlashAttribute("successMessage", "Mật khẩu mới đã được gửi vào email của bạn.");
            return "redirect:/login";
        } catch (Exception e) {
            log.error("Error processing forgot password for {}: {}", request.email(), e.getMessage(), e);
            // If it's a mail auth error, provide a clearer message than just 'Authentication failed'
            String errorMsg = e.getMessage();
            if (errorMsg != null && errorMsg.toLowerCase().contains("authentication failed")) {
                errorMsg = "Lỗi hệ thống: Không thể gửi email (Sai cấu hình Gmail).";
            }
            redirectAttributes.addFlashAttribute("errorMessage", errorMsg);
            return "redirect:/login";
        }
    }

    @GetMapping("/reset-password")
    public String showResetPasswordForm(Model model) {
        if (!model.containsAttribute("resetPasswordDto")) {
            model.addAttribute("resetPasswordDto", new ResetPasswordRequestDTO("", "", "", ""));
        }
        if (!model.containsAttribute("forgotPasswordDto")) {
            model.addAttribute("forgotPasswordDto", new ForgotPasswordRequestDTO(""));
        }
        return "authen/change-password";
    }

    @PostMapping("/reset-password")
    public String resetPassword(@Valid @ModelAttribute("resetPasswordDto") ResetPasswordRequestDTO request,
                                BindingResult bindingResult,
                                RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Thông tin không hợp lệ. Mật khẩu phải từ 6 ký tự.");
            return "redirect:/reset-password";
        }
        try {
            log.info("Processing reset password request for email: {}", request.email());
            if (!request.newPassword().equals(request.confirmPassword())) {
                throw new Exception("Mật khẩu xác nhận không khớp.");
            }
            authenService.resetPassword(request.email(), request.oldPassword(), request.newPassword());
            log.info("Password reset successful for email: {}", request.email());

            // Clear force change state
            org.springframework.web.context.request.RequestContextHolder.getRequestAttributes()
                .removeAttribute("forceChangeEmail",
                                org.springframework.web.context.request.RequestAttributes.SCOPE_SESSION);

            redirectAttributes.addFlashAttribute("successMessage", "Đổi mật khẩu thành công! Vui lòng đăng nhập lại.");
            return "redirect:/login";
        } catch (Exception e) {
            log.error("Error resetting password for {}: {}", request.email(), e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/reset-password";
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
