package fpt.legendcoffee.dto.app;

import lombok.Data;

@Data
public class AddressQueryDTO {
    private Integer districtId;
    private String wardCode;
    private Integer weight;      // gram - tổng cân nặng giỏ hàng
    private Long insuranceValue; // giá trị đơn hàng để tính phí bảo hiểm
}
