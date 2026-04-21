package fpt.legendcoffee.service.serviceImpl;

import java.security.SecureRandom;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import fpt.legendcoffee.common.exception.AuthenException;
import fpt.legendcoffee.dto.request.LoginRequestDTO;
import fpt.legendcoffee.dto.request.RegisterRequestDTO;
import fpt.legendcoffee.dto.response.ProfileDTO;
import fpt.legendcoffee.entity.User;
import fpt.legendcoffee.entity.enumeration.UserRole;
import fpt.legendcoffee.repository.UserRepository;
import fpt.legendcoffee.service.AuthenService;
import fpt.legendcoffee.service.MailService;
import jakarta.mail.MessagingException;
import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class AuthenServiceImpl implements AuthenService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final MailService mailService;

    @Override
    public void login(LoginRequestDTO request) {
        User user = userRepository.findByEmail(request.email()).orElse(null);
        if (user == null || !passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new AuthenException("Invalid email or password");
        } else if (!user.getIsActive()) {
            throw new AuthenException("User is inactive");
        }
    }

    @Override
    public boolean register(RegisterRequestDTO request) {
        if (userRepository.findByUsername(request.username()).isPresent()) {
            throw new AuthenException("Username already exists");
        }
        if (userRepository.findByEmail(request.email()).isPresent()) {
            throw new AuthenException("Email already exists");
        }
        if (userRepository.findByPhone(request.phone()).isPresent()) {
            throw new AuthenException("Phone number already exists");
        }

        User user = User.builder()
                .username(request.username())
                .password(passwordEncoder.encode(request.password()))
                .email(request.email())
                .phone(request.phone())
                .address(request.address())
                .role(UserRole.USER)
                .isActive(true)
                .build();

        userRepository.save(user);
        return true;
    }

    @Override
    public void resetPassword(String email, String oldPassword, String newPassword) {
        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null) {
            throw new AuthenException("User not found");
        }
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new AuthenException("Invalid old password");
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    @Override
    public void forgotPassword(String email) {
        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null) {
            throw new AuthenException("User not found");
        }
        String newPassword = generateRandomPassword();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        try {
            mailService.sendHtml(email, "Legend Coffee - Forgot Password", "<h1>Your new password is: " + newPassword + "</h1>");
        } catch (MessagingException e) {
            throw new AuthenException("Failed to send email");
        }
    }

    @Override
    public ProfileDTO getProfile(long id) {
        User user = userRepository.findById(id).orElse(null);
        if (user == null) {
            throw new AuthenException("User not found");
        }
        return ProfileDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .phone(user.getPhone())
                .address(user.getAddress())
                .build();
    }

    @Override
    public void updateProfile(long id, ProfileDTO profile) {
        User user = userRepository.findById(id).orElse(null);
        if (user == null) {
            throw new AuthenException("User not found");
        }
        user.setUsername(profile.getUsername());
        user.setEmail(profile.getEmail());
        user.setPhone(profile.getPhone());
        user.setAddress(profile.getAddress());
        userRepository.save(user);
    }

    //Helper method
    private String generateRandomPassword() {
        String chars = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
        SecureRandom rnd = new SecureRandom();
        StringBuilder sb = new StringBuilder(6);
        for (int i = 0; i < 6; i++)
            sb.append(chars.charAt(rnd.nextInt(chars.length())));
        return sb.toString();
    }

}
