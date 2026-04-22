package fpt.legendcoffee.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ResetPasswordRequestDTO(
        @NotBlank(message = "Email không được để trống") @Email(message = "Email không hợp lệ")
        String email,
        @NotBlank(message = "Mật khẩu cũ không được để trống") @Size(min = 6, message = "Mật khẩu phải từ 6 ký tự")
        String oldPassword,
        @NotBlank(message = "Mật khẩu mới không được để trống") @Size(min = 6, message = "Mật khẩu phải từ 6 ký tự")
        String newPassword,
        @NotBlank(message = "Xác nhận mật khẩu không được để trống") @Size(min = 6, message = "Mật khẩu phải từ 6 ký tự")
        String confirmPassword) {
}
