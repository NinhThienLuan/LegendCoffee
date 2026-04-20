package fpt.legendcoffee.dto.ghn;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class FeeResponseDTO {
    @JsonProperty("total")
    private Long total; // tổng phí (VNĐ)

    @JsonProperty("service_fee")
    private Long serviceFee;

    @JsonProperty("insurance_fee")
    private Long insuranceFee;

    @JsonProperty("cod_fee")
    private Long codFee;
}
