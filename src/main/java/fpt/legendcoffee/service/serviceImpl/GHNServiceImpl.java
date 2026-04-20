package fpt.legendcoffee.service.serviceImpl;

import fpt.legendcoffee.common.exception.GHNException;
import fpt.legendcoffee.common.properties.GHNProperties;
import fpt.legendcoffee.dto.ghn.*;
import fpt.legendcoffee.service.GHNService;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;


@Service
@Slf4j
public class GHNServiceImpl implements GHNService {

    private final RestTemplate restTemplate;
    private final GHNProperties props;

    public GHNServiceImpl(@Qualifier("ghnRestTemplate") RestTemplate restTemplate,
                          GHNProperties props) {
        this.restTemplate = restTemplate;
        this.props = props;
    }

    // ----------------------------------------------------------------
    // Tạo HttpHeaders chuẩn cho mọi request GHN
    // ----------------------------------------------------------------
    private HttpHeaders buildHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Token", props.getToken());
        return headers;
    }

    private HttpHeaders buildHeadersWithShopId() {
        HttpHeaders headers = buildHeaders();
        headers.set("ShopId", String.valueOf(props.getShopId()));
        return headers;
    }

    private String url(String path) {
        return props.getBaseUrl() + path;
    }

    // ----------------------------------------------------------------
    // Generic POST helper
    // ----------------------------------------------------------------
    private <T> T post(String path, Object body, ParameterizedTypeReference<GHNApiResponse<T>> typeRef,
                       boolean withShopId) {
        HttpHeaders headers = withShopId ? buildHeadersWithShopId() : buildHeaders();
        HttpEntity<Object> entity = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<GHNApiResponse<T>> response = restTemplate.exchange(
                    url(path), HttpMethod.POST, entity, typeRef);

            GHNApiResponse<T> apiResponse = response.getBody();
            if (apiResponse == null || !apiResponse.isSuccess()) {
                String msg = apiResponse != null ? apiResponse.getMessage() : "No response from GHN";
                log.error("[GHN] POST {} failed: {}", path, msg);
                throw new GHNException(apiResponse != null ? apiResponse.getCode() : -1, msg);
            }
            return apiResponse.getData();

        } catch (HttpClientErrorException ex) {
            log.error("[GHN] HTTP error calling {}: {} - {}", path, ex.getStatusCode(), ex.getResponseBodyAsString());
            throw new GHNException("GHN API lỗi: " + ex.getMessage());
        }
    }

    // ----------------------------------------------------------------
    // Generic GET helper
    // ----------------------------------------------------------------
    private <T> T get(String path, Map<String, ?> params,
                      ParameterizedTypeReference<GHNApiResponse<T>> typeRef, boolean withShopId) {
        HttpHeaders headers = withShopId ? buildHeadersWithShopId() : buildHeaders();

        // Note: GHN's GET APIs typically don't take a body. 
        // For simple GET without query params:
        HttpEntity<Void> entity = new HttpEntity<>(headers);
        
        // If params are needed for GET, they should be query parameters, 
        // but GHN Master Data often uses headers or is just a simple GET.
        // For District/Ward which need ID, GHN actually often uses POST or 
        // specific GET behavior that we might need to adjust.
        
        try {
            // If there are params, for GHN Master data, we might need to handle them differently.
            // But let's keep the exchange call structure.
            ResponseEntity<GHNApiResponse<T>> response = restTemplate.exchange(
                    url(path), HttpMethod.GET, entity, typeRef);

            GHNApiResponse<T> body = response.getBody();
            if (body == null || !body.isSuccess()) {
                throw new GHNException(body != null ? body.getMessage() : "No response");
            }
            return body.getData();

        } catch (HttpClientErrorException ex) {
            log.error("[GHN] HTTP error GET {}: {}", path, ex.getMessage());
            throw new GHNException("GHN API lỗi: " + ex.getMessage());
        }
    }

    // ================================================================
    // MASTER DATA
    // ================================================================

    @Override
    public List<ProvinceDTO> getProvinces() {
        log.info("[GHN] Fetching provinces");
        return get("/master-data/province", null,
                new ParameterizedTypeReference<GHNApiResponse<List<ProvinceDTO>>>() {}, false);
    }

    @Override
    public List<DistrictDTO> getDistricts(Integer provinceId) {
        log.info("[GHN] Fetching districts for provinceId={}", provinceId);
        // Map<String, Integer> params = Map.of("province_id", provinceId);
        // Note: GHN master-data/district actually usually uses POST with {"province_id": ...}
        // or GET with query params. Let's use POST as it's more reliable for GHN.
        return post("/master-data/district", Map.of("province_id", provinceId),
                new ParameterizedTypeReference<GHNApiResponse<List<DistrictDTO>>>() {}, false);
    }

    @Override
    public List<WardDTO> getWards(Integer districtId) {
        log.info("[GHN] Fetching wards for districtId={}", districtId);
        // GHN master-data/ward usually uses POST with {"district_id": ...}
        return post("/master-data/ward", Map.of("district_id", districtId),
                new ParameterizedTypeReference<GHNApiResponse<List<WardDTO>>>() {}, false);
    }

    // ================================================================
    // SHIPPING CALCULATION
    // ================================================================

    @Override
    public List<GHNServiceDTO> getAvailableServices(Integer fromDistrict, Integer toDistrict) {
        log.info("[GHN] Getting available services: {} -> {}", fromDistrict, toDistrict);
        Map<String, Integer> body = Map.of(
                "shop_id", props.getShopId(),
                "from_district", fromDistrict,
                "to_district", toDistrict
        );
        return post("/v2/shipping-order/available-services", body,
                new ParameterizedTypeReference<GHNApiResponse<List<GHNServiceDTO>>>() {}, false);
    }

    @Override
    public FeeResponseDTO calculateFee(FeeRequestDTO request) {
        log.info("[GHN] Calculating fee for service_id={}", request.getServiceId());
        return post("/v2/shipping-order/fee", request,
                new ParameterizedTypeReference<GHNApiResponse<FeeResponseDTO>>() {}, true);
    }

    @Override
    public LeadtimeResponseDTO getLeadtime(LeadtimeRequestDTO request) {
        log.info("[GHN] Getting leadtime for service_id={}", request.getServiceId());
        return post("/v2/shipping-order/leadtime", request,
                new ParameterizedTypeReference<GHNApiResponse<LeadtimeResponseDTO>>() {}, true);
    }

    // ================================================================
    // ORDER MANAGEMENT
    // ================================================================

    @Override
    public CreateOrderResponseDTO createOrder(CreateOrderRequestDTO request) {
        log.info("[GHN] Creating order for recipient={}", request.getToPhone());
        return post("/v2/shipping-order/create", request,
                new ParameterizedTypeReference<GHNApiResponse<CreateOrderResponseDTO>>() {}, true);
    }

    @Override
    public OrderDetailResponseDTO getOrderDetail(String orderCode) {
        log.info("[GHN] Getting order detail for orderCode={}", orderCode);
        Map<String, String> body = Map.of("order_code", orderCode);
        return post("/v2/shipping-order/detail", body,
                new ParameterizedTypeReference<GHNApiResponse<OrderDetailResponseDTO>>() {}, true);
    }

    @Override
    public boolean cancelOrder(List<String> orderCodes) {
        log.info("[GHN] Cancelling orders: {}", orderCodes);
        Map<String, List<String>> body = Map.of("order_codes", orderCodes);
        try {
            post("/v2/switch-status/cancel", body,
                    new ParameterizedTypeReference<GHNApiResponse<Object>>() {}, true);
            return true;
        } catch (GHNException e) {
            log.error("[GHN] Cancel order failed: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public boolean redeliverOrder(List<String> orderCodes) {
        log.info("[GHN] Redelivering orders: {}", orderCodes);
        Map<String, List<String>> body = Map.of("order_codes", orderCodes);
        try {
            post("/v2/switch-status/storing", body,
                    new ParameterizedTypeReference<GHNApiResponse<Object>>() {}, true);
            return true;
        } catch (GHNException e) {
            log.error("[GHN] Redeliver order failed: {}", e.getMessage());
            return false;
        }
    }

}

