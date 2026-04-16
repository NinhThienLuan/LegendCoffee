package fpt.legendcoffee.service.serviceImpl;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import fpt.legendcoffee.common.exception.AuthenException;
import fpt.legendcoffee.dto.LoginRequestDTO;
import fpt.legendcoffee.dto.RegisterRequestDTO;
import fpt.legendcoffee.entity.User;
import fpt.legendcoffee.entity.enumeration.UserRole;
import fpt.legendcoffee.repository.UserRepository;
import fpt.legendcoffee.service.AuthenService;
import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class AuthenServiceImpl implements AuthenService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void login(LoginRequestDTO request) {
        User user = userRepository.findByUsername(request.username()).orElse(null);
        if (user == null || !passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new AuthenException("Invalid username or password");
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

}
