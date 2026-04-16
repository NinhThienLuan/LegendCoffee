package fpt.legendcoffee.service.serviceImpl;

import java.util.Optional;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import fpt.legendcoffee.dto.LoginRequestDTO;
import fpt.legendcoffee.dto.RegisterRequestDTO;
import fpt.legendcoffee.entity.User;
import fpt.legendcoffee.repository.UserRepository;
import fpt.legendcoffee.service.AuthenService;
import lombok.AllArgsConstructor;
import fpt.legendcoffee.common.exception.AuthenException;

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
        throw new UnsupportedOperationException("Unimplemented method 'register'");
    }

}
