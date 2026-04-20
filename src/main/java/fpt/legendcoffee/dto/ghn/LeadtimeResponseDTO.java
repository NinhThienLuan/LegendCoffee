package fpt.legendcoffee.dto.ghn;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class LeadtimeResponseDTO {
    @JsonProperty("leadtime")
    private Long leadtime; // unix timestamp
}
