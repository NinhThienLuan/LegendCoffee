package fpt.legendcoffee.dto.ghn;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class CreateOrderResponseDTO {
    @JsonProperty("order_code")
    private String orderCode;

    @JsonProperty("sort_code")
    private String sortCode;

    @JsonProperty("total_fee")
    private Long totalFee;

    @JsonProperty("expected_delivery_time")
    private String expectedDeliveryTime; // ISO 8601
}
