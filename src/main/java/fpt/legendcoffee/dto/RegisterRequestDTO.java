package fpt.legendcoffee.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record RegisterRequestDTO(
        @NotBlank(message = "Username is required") String username,
        @NotBlank(message = "Password is required") String password,
        @NotBlank(message = "Email is required") @Email(message = "Email is invalid") String email,
        @NotBlank(message = "Phone is required") @Pattern(regexp = "\\d{10}", message = "Phone number must be 10 digits") String phone,
        @NotBlank(message = "Address is required") String address) {
}
