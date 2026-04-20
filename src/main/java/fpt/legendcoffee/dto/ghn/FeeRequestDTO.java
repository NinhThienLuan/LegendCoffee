package fpt.legendcoffee.dto.ghn;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class FeeRequestDTO {
    @JsonProperty("service_id")
    private Integer serviceId;

    @JsonProperty("from_district_id")
    private Integer fromDistrictId;

    @JsonProperty("from_ward_code")
    private String fromWardCode;

    @JsonProperty("to_district_id")
    private Integer toDistrictId;

    @JsonProperty("to_ward_code")
    private String toWardCode;

    @JsonProperty("weight")
    private Integer weight; // gram

    @JsonProperty("length")
    private Integer length; // cm

    @JsonProperty("width")
    private Integer width; // cm

    @JsonProperty("height")
    private Integer height; // cm

    @JsonProperty("insurance_value")
    private Long insuranceValue; // giá trị bảo hiểm (VNĐ)

    @JsonProperty("coupon")
    private String coupon;
}
