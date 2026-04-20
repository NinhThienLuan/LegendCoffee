package fpt.legendcoffee.dto.ghn;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class GHNServiceDTO {
    @JsonProperty("service_id")
    private Integer serviceId;

    @JsonProperty("short_name")
    private String shortName;

    @JsonProperty("service_type_id")
    private Integer serviceTypeId;
}
