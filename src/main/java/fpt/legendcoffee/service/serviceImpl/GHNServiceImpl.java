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
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;
import java.util.Map;
import java.util.Optional;


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
        
        String token = props.getToken() != null ? props.getToken().trim() : "";
        if (token.isEmpty()) {
            log.error("[GHN] Token is MISSING or empty! Check your application.properties.");
        } else {
            String masked = token.substring(0, Math.min(token.length(), 4)) + "..." + 
                            token.substring(Math.max(0, token.length() - 4));
            log.info("[GHN] Building headers with token: {}", masked);
        }
        
        headers.set("token", token);
        return headers;
    }

    private HttpHeaders buildHeadersWithShopId() {
        HttpHeaders headers = buildHeaders();
        headers.set("shopid", String.valueOf(props.getShopId()));
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
                int code = apiResponse != null ? apiResponse.getCode() : -1;
                String msg = apiResponse != null ? apiResponse.getMessage() : "No response from GHN";
                log.error("[GHN] POST {} failed: code={}, message={}", path, code, msg);
                throw new GHNException(code, msg);
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
        HttpEntity<Void> entity = new HttpEntity<>(headers);
        
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(url(path));
        if (params != null) {
            params.forEach(builder::queryParam);
        }
        log.info("[GHN] GET request: {}", builder.build().toUri());

        try {
            ResponseEntity<GHNApiResponse<T>> response = restTemplate.exchange(
                    builder.build().toUri(), HttpMethod.GET, entity, typeRef);

            GHNApiResponse<T> body = response.getBody();
            if (body == null || !body.isSuccess()) {
                int code = body != null ? body.getCode() : -1;
                String msg = body != null ? body.getMessage() : "No response";
                log.error("[GHN] GET {} failed: code={}, message={}", path, code, msg);
                throw new GHNException(code, msg);
            }
            return body.getData();

        } catch (HttpClientErrorException ex) {
            log.error("[GHN] HTTP error GET {}: {} - {}", path, ex.getStatusCode(), ex.getResponseBodyAsString());
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
        return get("/master-data/district", Map.of("province_id", provinceId),
                new ParameterizedTypeReference<GHNApiResponse<List<DistrictDTO>>>() {}, false);
    }

    @Override
    public List<WardDTO> getWards(Integer districtId) {
        log.info("[GHN] Fetching wards for districtId={}", districtId);
        return get("/master-data/ward", Map.of("district_id", districtId),
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
        log.info("[GHN] Calculating fee: serviceId={}, serviceTypeId={}, weight={}, fromDistrict={}, fromWard={}, toDistrict={}, toWard={}",
            request.getServiceId(), request.getServiceTypeId(), request.getWeight(),
            request.getFromDistrictId(), request.getFromWardCode(),
            request.getToDistrictId(), request.getToWardCode());

        try {
            return post("/v2/shipping-order/fee", request,
                new ParameterizedTypeReference<GHNApiResponse<FeeResponseDTO>>() {}, true);
        } catch (GHNException ex) {
            if (request.getServiceId() != null && request.getServiceTypeId() != null) {
            log.warn("[GHN] Fee failed with service_id={}, retrying with service_type_id={} only. Error={}",
                request.getServiceId(), request.getServiceTypeId(), ex.getMessage());

            FeeRequestDTO retryReq = FeeRequestDTO.builder()
                .serviceTypeId(request.getServiceTypeId())
                .fromDistrictId(request.getFromDistrictId())
                .fromWardCode(request.getFromWardCode())
                .toDistrictId(request.getToDistrictId())
                .toWardCode(request.getToWardCode())
                .weight(request.getWeight())
                .length(request.getLength())
                .width(request.getWidth())
                .height(request.getHeight())
                .insuranceValue(request.getInsuranceValue())
                .coupon(request.getCoupon())
                .build();

            return post("/v2/shipping-order/fee", retryReq,
                new ParameterizedTypeReference<GHNApiResponse<FeeResponseDTO>>() {}, true);
            }

            throw ex;
        }
    }

    @Override
    public LeadtimeResponseDTO getLeadtime(LeadtimeRequestDTO request) {
        log.info("[GHN] Getting leadtime for service_id={}", request.getServiceId());
        return post("/v2/shipping-order/leadtime", request,
                new ParameterizedTypeReference<GHNApiResponse<LeadtimeResponseDTO>>() {}, true);
    }

    @Override
    public GHNShopDTO getCurrentShopProfile() {
        GHNShopAllDataDTO data = get("/v2/shop/all", Map.of("offset", 0, "limit", 100),
                new ParameterizedTypeReference<GHNApiResponse<GHNShopAllDataDTO>>() {}, true);

        if (data == null || data.getShops() == null || data.getShops().isEmpty()) {
            throw new GHNException("Không lấy được thông tin shop GHN.");
        }

        Optional<GHNShopDTO> match = data.getShops().stream()
                .filter(s -> s.getId() != null && s.getId().equals(props.getShopId()))
                .findFirst();

        return match.orElseGet(() -> data.getShops().get(0));
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

