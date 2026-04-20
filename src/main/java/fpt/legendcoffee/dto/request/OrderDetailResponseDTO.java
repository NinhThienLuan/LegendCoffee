package fpt.legendcoffee.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class OrderDetailResponseDTO {
    @JsonProperty("order_code")
    private String orderCode;

    @JsonProperty("status")
    private String status;

    @JsonProperty("to_name")
    private String toName;

    @JsonProperty("to_phone")
    private String toPhone;

    @JsonProperty("to_address")
    private String toAddress;

    @JsonProperty("finish_date")
    private String finishDate;

    @JsonProperty("log")
    private List<LogDTO> log;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class LogDTO {
        @JsonProperty("status")
        private String status;

        @JsonProperty("updated_date")
        private String updatedDate;
    }
}
