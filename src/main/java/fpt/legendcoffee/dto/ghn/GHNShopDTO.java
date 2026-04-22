package fpt.legendcoffee.dto.ghn;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class GHNShopDTO {
    @JsonProperty("_id")
    private Integer id;

    @JsonProperty("name")
    private String name;

    @JsonProperty("phone")
    private String phone;

    @JsonProperty("address")
    private String address;

    @JsonProperty("ward_code")
    private String wardCode;

    @JsonProperty("district_id")
    private Integer districtId;
}
