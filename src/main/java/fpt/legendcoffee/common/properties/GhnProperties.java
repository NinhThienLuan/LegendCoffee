package fpt.legendcoffee.common.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "ghn.api")
public class GhnProperties {
    private String baseUrl;
    private String devUrl;
    private String token;
    private Integer shopId;
    private Integer fromDistrictId;
    private String fromWardCode;
    private int connectTimeout = 5000;
    private int readTimeout = 10000;

}
