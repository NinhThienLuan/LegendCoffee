package fpt.legendcoffee.common.util;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

/**
 * Tiện ích mã hóa VNPay.
 * Sử dụng HMAC-SHA512 để tạo và xác thực chữ ký giao dịch.
 */
public class VNPayHelper {

    private VNPayHelper() {
        // Utility class — không cho phép khởi tạo
    }

    /**
     * Tính HMAC-SHA512 của data với key cho trước.
     *
     * @param key  Secret key (vnp_HashSecret)
     * @param data Dữ liệu cần hash (encoded query string)
     * @return Chuỗi hex lowercase 128 ký tự
     */
    public static String hmacSHA512(String key, String data) {
        try {
            Mac mac = Mac.getInstance("HmacSHA512");
            SecretKeySpec secretKey = new SecretKeySpec(
                    key.getBytes(StandardCharsets.UTF_8),
                    "HmacSHA512"
            );
            mac.init(secretKey);
            byte[] hashBytes = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));

            // Chuyển bytes → hex string lowercase
            StringBuilder sb = new StringBuilder(hashBytes.length * 2);
            for (byte b : hashBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();

        } catch (Exception e) {
            throw new RuntimeException("Lỗi khi tính HMAC-SHA512", e);
        }
    }
}
