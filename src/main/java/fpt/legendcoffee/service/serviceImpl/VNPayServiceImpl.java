package fpt.legendcoffee.service.serviceImpl;

import fpt.legendcoffee.common.config.VNPayProperties;
import fpt.legendcoffee.service.VNPayService;
import fpt.legendcoffee.common.util.VNPayHelper;
import org.springframework.stereotype.Service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.TreeMap;

@Service
public class VNPayServiceImpl implements VNPayService {

    private static final ZoneId VIETNAM_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private final VNPayProperties properties;

    public VNPayServiceImpl(VNPayProperties properties) {
        this.properties = properties;
    }

    @Override
    public String createPaymentUrl(long amount, String txnRef, String orderInfo, String ipAddress) {
        ZonedDateTime now = ZonedDateTime.now(VIETNAM_ZONE);

        // Bước 1: TreeMap tự sắp xếp theo key — VNPay yêu cầu thứ tự alphabet
        TreeMap<String, String> vnpParams = new TreeMap<>();
        vnpParams.put("vnp_Version", "2.1.0");
        vnpParams.put("vnp_Command", "pay");
        vnpParams.put("vnp_TmnCode", properties.getTmnCode().trim());
        vnpParams.put("vnp_Amount", String.valueOf(amount * 100)); // VNPay tính x100
        vnpParams.put("vnp_CreateDate", now.format(DATE_FORMATTER));
        vnpParams.put("vnp_CurrCode", "VND");
        vnpParams.put("vnp_IpAddr", ipAddress);
        vnpParams.put("vnp_Locale", "vn");
        vnpParams.put("vnp_OrderInfo", orderInfo);
        vnpParams.put("vnp_OrderType", "other");
        vnpParams.put("vnp_ReturnUrl", properties.getReturnUrl().trim());
        vnpParams.put("vnp_TxnRef", txnRef);
        vnpParams.put("vnp_ExpireDate", now.plusMinutes(15).format(DATE_FORMATTER));

        // Bước 2: Build encoded query string
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> entry : vnpParams.entrySet()) {
            if (entry.getValue() != null && !entry.getValue().isEmpty()) {
                sb.append(urlEncode(entry.getKey()))
                        .append('=')
                        .append(urlEncode(entry.getValue()))
                        .append('&');
            }
        }

        // Bước 3: Hash data = encoded string (bỏ "&" cuối)
        String hashData = sb.toString().replaceAll("&$", "");
        String secureHash = VNPayHelper.hmacSHA512(properties.getHashSecret().trim(), hashData);

        // Bước 4: URL cuối = PayUrl + encoded query string + vnp_SecureHash
        return properties.getPayUrl() + "?" + hashData + "&vnp_SecureHash=" + secureHash;
    }

    @Override
    public boolean validateSignature(Map<String, String> queryParams) {
        // Lấy các param vnp_ (trừ SecureHash) và sắp xếp theo alphabet
        TreeMap<String, String> sortedParams = new TreeMap<>();
        for (Map.Entry<String, String> entry : queryParams.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            if (value != null && !value.isEmpty()
                    && key.startsWith("vnp_")
                    && !key.equals("vnp_SecureHash")
                    && !key.equals("vnp_SecureHashType")) {
                sortedParams.put(key, value);
            }
        }

        // Build hash data giống với cách làm createPaymentUrl
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> entry : sortedParams.entrySet()) {
            sb.append(urlEncode(entry.getKey()))
                    .append('=')
                    .append(urlEncode(entry.getValue()))
                    .append('&');
        }

        String hashData = sb.toString().replaceAll("&$", "");
        String computedHash = VNPayHelper.hmacSHA512(properties.getHashSecret().trim(), hashData);
        String receivedHash = queryParams.get("vnp_SecureHash");

        return receivedHash != null && computedHash.equalsIgnoreCase(receivedHash);
    }

    /**
     * URL encode theo chuẩn application/x-www-form-urlencoded (space → "+").
     */
    private String urlEncode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
