package fpt.legendcoffee.service;

import fpt.legendcoffee.dto.app.*;
import fpt.legendcoffee.dto.ghn.DistrictDTO;
import fpt.legendcoffee.dto.ghn.ProvinceDTO;
import fpt.legendcoffee.dto.ghn.WardDTO;
import fpt.legendcoffee.entity.Order;
import fpt.legendcoffee.entity.ShippingInfo;

import java.util.List;

/**
 * Tầng nghiệp vụ - phối hợp GHNService + Repository
 */
public interface ShippingService {

    // Master data (có cache)
    List<ProvinceDTO> getProvinces();
    List<DistrictDTO> getDistricts(Integer provinceId);
    List<WardDTO> getWards(Integer districtId);

    // Tính phí & thời gian giao — gọi song song cho tất cả service
    List<ShippingOptionDTO> getShippingOptions(AddressQueryDTO query);

    // Lưu thông tin vận chuyển vào DB (chưa đẩy qua GHN)
    ShippingInfo saveShippingInfo(CheckoutRequestDTO checkout, Order order);

    // Đẩy đơn hàng sang GHN sau khi thanh toán thành công
    ShippingInfo pushOrderToGHN(Long orderId);

    // Tracking
    OrderStatusDTO getOrderStatus(Long orderId);
    OrderStatusDTO getOrderStatusByGHNCode(String ghnOrderCode);

    // Webhook handler
    void handleWebhook(String ghnOrderCode, String newStatus);

    // Huỷ đơn (gọi khi user cancel order)
    boolean cancelShipping(Long orderId);
}
