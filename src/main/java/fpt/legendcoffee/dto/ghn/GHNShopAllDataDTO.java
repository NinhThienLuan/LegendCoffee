package fpt.legendcoffee.dto.ghn;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class GHNShopAllDataDTO {
    @JsonProperty("last_offset")
    private Integer lastOffset;

    @JsonProperty("shops")
    private List<GHNShopDTO> shops;
}
