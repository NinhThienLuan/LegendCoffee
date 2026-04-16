package fpt.legendcoffee.common.dataInit;

import fpt.legendcoffee.entity.User;
import fpt.legendcoffee.entity.enumeration.UserRole;
import fpt.legendcoffee.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class DataInit implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        final String defaultPassword = "password123";

        // Admin account
        if (userRepository.findByUsername("admin").isEmpty()) {
            User admin = User.builder()
                    .username("admin")
                    .password(passwordEncoder.encode(defaultPassword))
                    .email("admin@legendcoffee.com")
                    .phone("0123456789")
                    .address("Legend Coffee HQ")
                    .role(UserRole.ADMIN)
                    .isActive(true)
                    .build();
            userRepository.save(admin);
        }

        // Standard user account
        if (userRepository.findByUsername("user").isEmpty()) {
            User user = User.builder()
                    .username("user")
                    .password(passwordEncoder.encode(defaultPassword))
                    .email("user@gmail.com")
                    .phone("0987654321")
                    .address("123 Street")
                    .role(UserRole.USER)
                    .isActive(true)
                    .build();
            userRepository.save(user);
        }
    }
}
