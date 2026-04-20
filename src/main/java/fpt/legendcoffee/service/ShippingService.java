package fpt.legendcoffee.service;

import fpt.legendcoffee.dto.app.*;
import fpt.legendcoffee.dto.ghn.DistrictDTO;
import fpt.legendcoffee.dto.ghn.ProvinceDTO;
import fpt.legendcoffee.dto.ghn.WardDTO;
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

    // Tạo đơn GHN sau khi đơn hàng đã được lưu vào DB
    ShippingInfo createGHNOrder(CheckoutRequestDTO checkout);

    // Tracking
    OrderStatusDTO getOrderStatus(Long orderId);
    OrderStatusDTO getOrderStatusByGHNCode(String ghnOrderCode);

    // Webhook handler
    void handleWebhook(String ghnOrderCode, String newStatus);

    // Huỷ đơn (gọi khi user cancel order)
    boolean cancelShipping(Long orderId);
}
