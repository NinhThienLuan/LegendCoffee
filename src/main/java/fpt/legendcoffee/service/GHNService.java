package fpt.legendcoffee.service;

import fpt.legendcoffee.dto.ghn.*;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface GHNService {

    // --- Master Data ---
    List<ProvinceDTO> getProvinces();
    List<DistrictDTO> getDistricts(Integer provinceId);
    List<WardDTO> getWards(Integer districtId);

    // --- Shipping calculation ---
    List<GHNServiceDTO> getAvailableServices(Integer fromDistrict, Integer toDistrict);
    FeeResponseDTO calculateFee(FeeRequestDTO request);
    LeadtimeResponseDTO getLeadtime(LeadtimeRequestDTO request);
    GHNShopDTO getCurrentShopProfile();

    // --- Order management ---
    CreateOrderResponseDTO createOrder(CreateOrderRequestDTO request);
    OrderDetailResponseDTO getOrderDetail(String orderCode);
    boolean cancelOrder(List<String> orderCodes);
    boolean redeliverOrder(List<String> orderCodes);
}

