package fpt.legendcoffee.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterRequestDTO(
        @NotBlank(message = "Username is required") 
        String username,

        @Size(min = 6, message = "Password must be at least 6 characters") 
        @NotBlank(message = "Password is required") 
        String password,
        
        @NotBlank(message = "Email is required") 
        @Email(message = "Email is invalid") 
        String email,
        
        @NotBlank(message = "Phone is r equired") 
        @Pattern(regexp = "\\d{10}", message = "Phone number must be 10 digits") 
        String phone,
        
        @NotBlank(message = "Address is required") 
        String address) {
}
