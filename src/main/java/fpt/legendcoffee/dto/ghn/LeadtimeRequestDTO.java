package fpt.legendcoffee.dto.ghn;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LeadtimeRequestDTO {
    @JsonProperty("from_district_id")
    private Integer fromDistrictId;

    @JsonProperty("from_ward_code")
    private String fromWardCode;

    @JsonProperty("to_district_id")
    private Integer toDistrictId;

    @JsonProperty("to_ward_code")
    private String toWardCode;

    @JsonProperty("service_id")
    private Integer serviceId;
}
