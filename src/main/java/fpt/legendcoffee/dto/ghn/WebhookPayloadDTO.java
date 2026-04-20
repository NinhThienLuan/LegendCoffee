package fpt.legendcoffee.dto.ghn;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class WebhookPayloadDTO {
    @JsonProperty("CODAmount")
    private Long codAmount;

    @JsonProperty("CODTransferDate")
    private String codTransferDate;

    @JsonProperty("Description")
    private String description;

    @JsonProperty("Fee")
    private Long fee;

    @JsonProperty("OrderCode")
    private String orderCode;

    @JsonProperty("PaymentTypeID")
    private Integer paymentTypeId;

    @JsonProperty("ShopID")
    private Integer shopId;

    @JsonProperty("Status")
    private String status;

    @JsonProperty("Time")
    private String time;

    @JsonProperty("Type")
    private String type;
}
