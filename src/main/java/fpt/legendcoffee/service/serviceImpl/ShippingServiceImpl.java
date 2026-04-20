package fpt.legendcoffee.service.serviceImpl;

import fpt.legendcoffee.common.exception.GHNException;
import fpt.legendcoffee.common.properties.GHNProperties;
import fpt.legendcoffee.dto.app.*;
import fpt.legendcoffee.dto.ghn.*;
import fpt.legendcoffee.entity.Order;
import fpt.legendcoffee.entity.ShippingInfo;
import fpt.legendcoffee.repository.OrderRepository;
import fpt.legendcoffee.repository.ShippingInfoRepository;
import fpt.legendcoffee.service.GHNService;
import fpt.legendcoffee.service.ShippingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
@RequiredArgsConstructor
public class ShippingServiceImpl implements ShippingService {

    private final GHNService ghnService;
    private final ShippingInfoRepository shippingInfoRepository;
    private final OrderRepository orderRepository;
    private final GHNProperties props;

    // ================================================================
    // MASTER DATA — dùng @Cacheable để tránh gọi lặp lại
    // Cần thêm @EnableCaching vào main class và cấu hình cache
    // ================================================================

    @Override
    @Cacheable("ghn-provinces")
    public List<ProvinceDTO> getProvinces() {
        return ghnService.getProvinces();
    }

    @Override
    @Cacheable(value = "ghn-districts", key = "#provinceId")
    public List<DistrictDTO> getDistricts(Integer provinceId) {
        return ghnService.getDistricts(provinceId);
    }

    @Override
    @Cacheable(value = "ghn-wards", key = "#districtId")
    public List<WardDTO> getWards(Integer districtId) {
        return ghnService.getWards(districtId);
    }

    // ================================================================
    // TÍNH PHÍ SHIP — gọi song song tất cả dịch vụ bằng CompletableFuture
    // ================================================================

    @Override
    public List<ShippingOptionDTO> getShippingOptions(AddressQueryDTO query) {
        // 1. Lấy danh sách dịch vụ khả dụng từ kho -> đến địa chỉ giao
        List<GHNServiceDTO> services = ghnService.getAvailableServices(
                props.getFromDistrictId(), query.getDistrictId());

        if (services == null || services.isEmpty()) {
            return List.of();
        }

        // 2. Gọi fee + leadtime song song cho từng dịch vụ (CompletableFuture)
        List<CompletableFuture<ShippingOptionDTO>> futures = services.stream()
                .map(service -> CompletableFuture.supplyAsync(() ->
                        buildShippingOption(service, query)))
                .toList();

        // 3. Chờ tất cả hoàn thành và gộp kết quả
        List<ShippingOptionDTO> options = new ArrayList<>();
        for (CompletableFuture<ShippingOptionDTO> future : futures) {
            try {
                ShippingOptionDTO opt = future.get();
                if (opt != null) options.add(opt);
            } catch (Exception e) {
                log.warn("[Shipping] Failed to get option for one service: {}", e.getMessage());
            }
        }

        return options;
    }

    private ShippingOptionDTO buildShippingOption(GHNServiceDTO service, AddressQueryDTO query) {
        try {
            // Gọi fee
            FeeRequestDTO feeReq = FeeRequestDTO.builder()
                    .serviceId(service.getServiceId())
                    .fromDistrictId(props.getFromDistrictId())
                    .fromWardCode(props.getFromWardCode())
                    .toDistrictId(query.getDistrictId())
                    .toWardCode(query.getWardCode())
                    .weight(query.getWeight() != null ? query.getWeight() : 500)
                    .insuranceValue(query.getInsuranceValue())
                    .build();

            // Gọi leadtime
            LeadtimeRequestDTO leadReq = LeadtimeRequestDTO.builder()
                    .serviceId(service.getServiceId())
                    .fromDistrictId(props.getFromDistrictId())
                    .fromWardCode(props.getFromWardCode())
                    .toDistrictId(query.getDistrictId())
                    .toWardCode(query.getWardCode())
                    .build();

            // Gọi song song fee + leadtime cho service này
            CompletableFuture<FeeResponseDTO> feeFuture =
                    CompletableFuture.supplyAsync(() -> ghnService.calculateFee(feeReq));
            CompletableFuture<LeadtimeResponseDTO> leadFuture =
                    CompletableFuture.supplyAsync(() -> ghnService.getLeadtime(leadReq));

            FeeResponseDTO fee = feeFuture.get();
            LeadtimeResponseDTO lead = leadFuture.get();

            LocalDateTime leadtime = lead.getLeadtime() != null
                    ? LocalDateTime.ofInstant(Instant.ofEpochSecond(lead.getLeadtime()),
                                              ZoneId.of("Asia/Ho_Chi_Minh"))
                    : null;

            return ShippingOptionDTO.builder()
                    .serviceId(service.getServiceId())
                    .serviceName(mapServiceName(service.getServiceTypeId()))
                    .shippingFee(fee.getTotal())
                    .leadtime(leadtime)
                    .build();

        } catch (Exception e) {
            log.error("[Shipping] Error calculating option for serviceId={}: {}",
                    service.getServiceId(), e.getMessage());
            return null;
        }
    }

    private String mapServiceName(Integer serviceTypeId) {
        return switch (serviceTypeId) {
            case 1 -> "Giao nhanh";
            case 2 -> "Giao chuẩn";
            case 3 -> "Tiết kiệm";
            default -> "Vận chuyển";
        };
    }

    // ================================================================
    // TẠO ĐƠN GHN — gọi từ backend sau khi đã lưu order vào DB
    // ================================================================

    @Override
    @Transactional
    public ShippingInfo createGHNOrder(CheckoutRequestDTO checkout) {
        // Lấy đối tượng Order từ DB
        Order order = orderRepository.findById(checkout.getOrderId())
                .orElseThrow(() -> new GHNException("Không tìm thấy đơn hàng với ID: " + checkout.getOrderId()));

        // Tránh tạo đơn trùng cho cùng một order
        if (shippingInfoRepository.existsByOrderId(checkout.getOrderId())) {
            throw new GHNException("Đơn hàng này đã được tạo vận chuyển trước đó");
        }

        // Tạo request cho GHN
        CreateOrderRequestDTO createReq = CreateOrderRequestDTO.builder()
                .toName(checkout.getRecipientName())
                .toPhone(checkout.getRecipientPhone())
                .toAddress(checkout.getRecipientAddress())
                .toWardName(checkout.getWardName())
                .toDistrictName(checkout.getDistrictName())
                .toProvinceName(checkout.getProvinceName())
                .toWardCode(checkout.getWardCode())
                .toDistrictId(checkout.getDistrictId())
                .serviceId(checkout.getServiceId())
                .paymentTypeId(checkout.getPaymentTypeId())
                .weight(500)   // TODO: tính từ giỏ hàng thực tế
                .length(20)
                .width(20)
                .height(10)
                .insuranceValue(0L)
                .codAmount(checkout.getPaymentTypeId() == 2 ? checkout.getShippingFee() : 0L)
                .note(checkout.getNote())
                .requiredNote("CHOTHUHANG")
                .build();

        // Gọi GHN API tạo đơn
        CreateOrderResponseDTO ghnResponse = ghnService.createOrder(createReq);
        log.info("[Shipping] GHN order created: {}", ghnResponse.getOrderCode());

        // Parse thời gian giao dự kiến
        LocalDateTime expectedTime = null;
        if (ghnResponse.getExpectedDeliveryTime() != null) {
            try {
                expectedTime = LocalDateTime.parse(
                        ghnResponse.getExpectedDeliveryTime()
                                .replace("Z", "")
                                .replace("T", "T"));
            } catch (Exception ex) {
                log.warn("[Shipping] Cannot parse expectedDeliveryTime: {}",
                        ghnResponse.getExpectedDeliveryTime());
            }
        }

        // Lưu thông tin vận chuyển vào DB
        // Lưu ý: createdAt / updatedAt được quản lý tự động bởi BaseEntity (JPA Auditing)
        ShippingInfo info = ShippingInfo.builder()
                .order(order)                                        // @OneToOne với Order
                .ghnOrderCode(ghnResponse.getOrderCode())
                .recipientName(checkout.getRecipientName())
                .recipientPhone(checkout.getRecipientPhone())
                .recipientAddress(checkout.getRecipientAddress())
                .provinceId(checkout.getProvinceId())
                .provinceName(checkout.getProvinceName())
                .districtId(checkout.getDistrictId())
                .districtName(checkout.getDistrictName())
                .wardCode(checkout.getWardCode())
                .wardName(checkout.getWardName())
                .serviceId(checkout.getServiceId())
                .serviceName(checkout.getServiceName())
                .shippingFee(ghnResponse.getTotalFee())
                .paymentTypeId(checkout.getPaymentTypeId())
                .status("ready_to_pick")
                .expectedDeliveryTime(expectedTime)
                .note(checkout.getNote())
                .build();

        return shippingInfoRepository.save(info);
    }

    // ================================================================
    // TRACKING
    // ================================================================

    @Override
    public OrderStatusDTO getOrderStatus(Long orderId) {
        ShippingInfo info = shippingInfoRepository.findByOrderId(orderId)
                .orElseThrow(() -> new GHNException(
                        "Không tìm thấy thông tin vận chuyển cho đơn " + orderId));
        return getOrderStatusByGHNCode(info.getGhnOrderCode());
    }

    @Override
    public OrderStatusDTO getOrderStatusByGHNCode(String ghnOrderCode) {
        OrderDetailResponseDTO detail = ghnService.getOrderDetail(ghnOrderCode);

        List<OrderStatusDTO.LogItemDTO> logs = new ArrayList<>();
        if (detail.getLog() != null) {
            detail.getLog().forEach(l -> logs.add(OrderStatusDTO.LogItemDTO.builder()
                    .status(l.getStatus())
                    .statusLabel(OrderStatusDTO.mapStatusLabel(l.getStatus()))
                    .time(l.getUpdatedDate())
                    .build()));
        }

        return OrderStatusDTO.builder()
                .ghnOrderCode(ghnOrderCode)
                .status(detail.getStatus())
                .statusLabel(OrderStatusDTO.mapStatusLabel(detail.getStatus()))
                .toName(detail.getToName())
                .toPhone(detail.getToPhone())
                .toAddress(detail.getToAddress())
                .finishDate(detail.getFinishDate())
                .logs(logs)
                .build();
    }

    // ================================================================
    // WEBHOOK — cập nhật trạng thái khi GHN gửi notification
    // ================================================================

    @Override
    @Transactional
    public void handleWebhook(String ghnOrderCode, String newStatus) {
        log.info("[Webhook] GHN update: orderCode={}, status={}", ghnOrderCode, newStatus);
        int updated = shippingInfoRepository.updateStatusByGhnOrderCode(ghnOrderCode, newStatus);
        if (updated == 0) {
            log.warn("[Webhook] No shipping record found for orderCode={}", ghnOrderCode);
        }
        // TODO: Gửi email/notification cho khách hàng tại đây
    }

    // ================================================================
    // HUỶ ĐƠN
    // ================================================================

    @Override
    @Transactional
    public boolean cancelShipping(Long orderId) {
        ShippingInfo info = shippingInfoRepository.findByOrderId(orderId)
                .orElseThrow(() -> new GHNException("Không tìm thấy thông tin vận chuyển"));

        boolean success = ghnService.cancelOrder(List.of(info.getGhnOrderCode()));
        if (success) {
            info.setStatus("cancel");
            shippingInfoRepository.save(info);
            log.info("[Shipping] Order {} cancelled successfully", info.getGhnOrderCode());
        }
        return success;
    }
}
