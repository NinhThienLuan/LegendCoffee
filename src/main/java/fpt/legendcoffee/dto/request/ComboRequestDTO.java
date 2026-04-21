package fpt.legendcoffee.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ComboRequestDTO {

    @NotBlank(message = "Tên combo không được để trống")
    private String name;

    private String description;

    @NotNull(message = "Giá combo không được null")
    @DecimalMin(value = "0.0", inclusive = false, message = "Giá combo phải > 0")
    private BigDecimal price;

    @NotNull(message = "Ngày bắt đầu không được null")
    private LocalDateTime startDate;

    @NotNull(message = "Ngày kết thúc không được null")
    private LocalDateTime endDate;

    private Boolean isActive = true;

    @NotEmpty(message = "Combo phải có ít nhất một sản phẩm biến thể")
    @Valid
    private List<ComboItemRequestDTO> items = new ArrayList<>();

    public void validate() {
        if (startDate != null && endDate != null && endDate.isBefore(startDate)) {
            throw new IllegalArgumentException("Ngày kết thúc phải sau ngày bắt đầu");
        }
    }
}

