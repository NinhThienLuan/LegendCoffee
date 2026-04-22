package fpt.legendcoffee.common.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "ghn.api")
public class GHNProperties {
    private String baseUrl;
    private String devUrl;
    private String token;
    private Integer shopId;
    private String fromName;
    private String fromPhone;
    private String fromAddress;
    private String fromWardName;
    private String fromDistrictName;
    private String fromProvinceName;
    private Integer fromDistrictId;
    private String fromWardCode;
    private int connectTimeout = 5000;
    private int readTimeout = 10000;

}
