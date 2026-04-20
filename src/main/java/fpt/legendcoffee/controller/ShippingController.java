package fpt.legendcoffee.controller;

import fpt.legendcoffee.dto.app.AddressQueryDTO;
import fpt.legendcoffee.dto.app.OrderStatusDTO;
import fpt.legendcoffee.dto.app.ShippingOptionDTO;
import fpt.legendcoffee.dto.ghn.DistrictDTO;
import fpt.legendcoffee.dto.ghn.ProvinceDTO;
import fpt.legendcoffee.dto.ghn.WardDTO;
import fpt.legendcoffee.service.ShippingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller phục vụ AJAX từ trang checkout (Thymeleaf + fetch/axios).
 *
 * Các endpoint này được gọi phía client-side để:
 *  - Tải danh sách tỉnh/thành phố
 *  - Tải danh sách quận/huyện khi chọn tỉnh
 *  - Tải danh sách phường/xã khi chọn quận
 *  - Tính phí vận chuyển sau khi chọn xong địa chỉ
 *  - Tra cứu trạng thái đơn hàng
 */
@RestController
@RequestMapping("/api/shipping")
@RequiredArgsConstructor
public class ShippingController {

    private final ShippingService shippingService;

    /**
     * GET /api/shipping/provinces
     * Trả về danh sách tỉnh/thành phố.
     */
    @GetMapping("/provinces")
    public ResponseEntity<List<ProvinceDTO>> getProvinces() {
        return ResponseEntity.ok(shippingService.getProvinces());
    }

    /**
     * GET /api/shipping/districts?provinceId=201
     * Trả về danh sách quận/huyện — gọi khi user chọn tỉnh.
     */
    @GetMapping("/districts")
    public ResponseEntity<List<DistrictDTO>> getDistricts(@RequestParam Integer provinceId) {
        return ResponseEntity.ok(shippingService.getDistricts(provinceId));
    }

    /**
     * GET /api/shipping/wards?districtId=1442
     * Trả về danh sách phường/xã — gọi khi user chọn quận.
     */
    @GetMapping("/wards")
    public ResponseEntity<List<WardDTO>> getWards(@RequestParam Integer districtId) {
        return ResponseEntity.ok(shippingService.getWards(districtId));
    }

    /**
     * POST /api/shipping/options
     * Tính phí và thời gian giao cho tất cả dịch vụ — gọi khi user chọn xong địa chỉ.
     *
     * Body: { "districtId": 1442, "wardCode": "20308", "weight": 500, "insuranceValue": 0 }
     */
    @PostMapping("/options")
    public ResponseEntity<List<ShippingOptionDTO>> getShippingOptions(
            @RequestBody AddressQueryDTO query) {
        return ResponseEntity.ok(shippingService.getShippingOptions(query));
    }

    /**
     * GET /api/shipping/track/{orderId}
     * Xem trạng thái vận chuyển theo orderId trong hệ thống.
     */
    @GetMapping("/track/{orderId}")
    public ResponseEntity<OrderStatusDTO> trackOrder(@PathVariable Long orderId) {
        return ResponseEntity.ok(shippingService.getOrderStatus(orderId));
    }
}
