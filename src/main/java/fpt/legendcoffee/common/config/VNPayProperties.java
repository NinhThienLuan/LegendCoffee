package fpt.legendcoffee.common.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Cấu hình VNPay — load từ application.properties với prefix "vnpay".
 * vnpay.tmn-code, vnpay.hash-secret, ...
 */
@Data
@Component
@ConfigurationProperties(prefix = "vnpay")
public class VNPayProperties {

    private String tmnCode;

    private String hashSecret;

    private String payUrl;
    private String returnUrl;

    private String ipnUrl;
}
