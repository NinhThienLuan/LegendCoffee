package fpt.legendcoffee.dto.ghn;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class CreateOrderRequestDTO {
    // Thông tin người nhận
    @JsonProperty("to_name")
    private String toName;

    @JsonProperty("to_phone")
    private String toPhone;

    @JsonProperty("to_address")
    private String toAddress;

    @JsonProperty("to_ward_name")
    private String toWardName;

    @JsonProperty("to_district_name")
    private String toDistrictName;

    @JsonProperty("to_province_name")
    private String toProvinceName;

    @JsonProperty("to_ward_code")
    private String toWardCode;

    @JsonProperty("to_district_id")
    private Integer toDistrictId;

    // Thông tin dịch vụ
    @JsonProperty("service_id")
    private Integer serviceId;

    @JsonProperty("service_type_id")
    private Integer serviceTypeId;

    // payment_type_id: 1 = shop trả ship, 2 = người nhận trả (COD)
    @JsonProperty("payment_type_id")
    private Integer paymentTypeId;

    // Thông tin hàng hoá (cà phê)
    @JsonProperty("weight")
    private Integer weight; // gram

    @JsonProperty("length")
    private Integer length;

    @JsonProperty("width")
    private Integer width;

    @JsonProperty("height")
    private Integer height;

    @JsonProperty("insurance_value")
    private Long insuranceValue;

    // Tiền thu hộ COD (nếu có)
    @JsonProperty("cod_amount")
    private Long codAmount;

    @JsonProperty("note")
    private String note;

    @JsonProperty("required_note")
    private String requiredNote; // CHOTHUHANG | CHOXEMHANGKHONGTHU | KHONGCHOXEMHANG

    @JsonProperty("items")
    private List<OrderItemDTO> items;

    @Data
    @Builder
    public static class OrderItemDTO {
        @JsonProperty("name")
        private String name;

        @JsonProperty("quantity")
        private Integer quantity;

        @JsonProperty("weight")
        private Integer weight;
    }
}
