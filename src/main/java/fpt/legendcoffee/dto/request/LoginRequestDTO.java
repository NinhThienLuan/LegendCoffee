package fpt.legendcoffee.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginRequestDTO(
        @NotBlank(message = "Email is required")
        @Email(message = "Email is invalid") String email,
        @Size(min = 6, message = "Password must be at least 6 characters")
        @NotBlank(message = "Password is required") String password) {
}
