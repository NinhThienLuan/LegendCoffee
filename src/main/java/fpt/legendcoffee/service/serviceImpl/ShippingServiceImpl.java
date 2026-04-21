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
                .serviceTypeId(2) // Bắt buộc truyền 2 (Giao chuẩn) để tránh lỗi lệch serviceId của tuyến đường
                .paymentTypeId(checkout.getPaymentTypeId())
                .weight(500)   // TODO: tính từ giỏ hàng thực tế
                .length(20)
                .width(20)
                .height(10)
                .insuranceValue(0L)
                .codAmount(checkout.getPaymentTypeId() == 2 ? checkout.getShippingFee() : 0L)
                .note(checkout.getNote())
                .requiredNote("CHOTHUHANG")
                // BUG GHN "Tên hàng hoá bắt buộc": Thêm mock item cho đến khi tích hợp với cart thật
                .items(List.of(
                        CreateOrderRequestDTO.OrderItemDTO.builder()
                                .name("Đơn hàng Legend Coffee") // Tên bắt buộc
                                .quantity(1)                    // Số lượng bắt buộc
                                .weight(500)                    // Cân nặng bắt buộc
                                .build()
                ))
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
        // 1. Lấy thông tin từ GHN
        OrderDetailResponseDTO detail = ghnService.getOrderDetail(ghnOrderCode);

        // 2. Lấy status "Sự thật" từ Database
        ShippingInfo info = shippingInfoRepository.findByGhnOrderCode(ghnOrderCode).orElse(null);
        String rawStatus = (info != null) ? info.getStatus() : detail.getStatus();
        String finalStatus = rawStatus.toLowerCase();

        // 3. Tự dựng "Cây lịch trình" linh hoạt
        List<OrderStatusDTO.LogItemDTO> timeline = new ArrayList<>();
        String updatedAt = (info != null) ? info.getUpdatedAt().toString() : "Vừa xong";
        String createdAt = (info != null) ? info.getCreatedAt().toString() : updatedAt;

        // BƯỚC 1: Mặc định luôn có
        timeline.add(OrderStatusDTO.LogItemDTO.builder()
                .status("ready_to_pick")
                .statusLabel("Đơn hàng đã được khởi tạo thành công")
                .time(createdAt)
                .build());

        // BƯỚC 2: Đang vận chuyển (Nếu status thuộc nhóm đang giao hoặc đã xong)
        boolean isDelivering = finalStatus.contains("deliv") || finalStatus.contains("transport") || finalStatus.contains("picking");
        boolean isFinished = finalStatus.contains("delivered") || finalStatus.contains("received") || finalStatus.contains("finish");

        if (isDelivering || isFinished) {
            timeline.add(OrderStatusDTO.LogItemDTO.builder()
                    .status("delivering")
                    .statusLabel("Đơn hàng đang trên đường vận chuyển")
                    .time(updatedAt)
                    .build());
        }

        // BƯỚC 3: Hoàn tất
        if (isFinished) {
            timeline.add(OrderStatusDTO.LogItemDTO.builder()
                    .status("delivered")
                    .statusLabel("Đã giao hàng thành công")
                    .time(updatedAt)
                    .build());
        }

        // Đảo ngược để mốc MỚI NHẤT lên ĐẦU trang (Standard UI)
        java.util.Collections.reverse(timeline);

        return OrderStatusDTO.builder()
                .ghnOrderCode(ghnOrderCode)
                .status(rawStatus)
                .statusLabel(OrderStatusDTO.mapStatusLabel(rawStatus))
                .toName(detail.getToName())
                .toPhone(detail.getToPhone())
                .toAddress(detail.getToAddress())
                .finishDate(detail.getFinishDate())
                .logs(timeline)
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

        // Kiểm tra trạng thái trong DB: chỉ cho phép huỷ khi còn ở trạng thái 'ready_to_pick'
        // Tránh phụ thuộc hoàn toàn vào API GHN nếu trạng thái trên hệ thống đối tác chưa cập nhật kịp
        if (!"ready_to_pick".equalsIgnoreCase(info.getStatus())) {
            log.warn("[Shipping] Không thể huỷ đơn {} vì trạng thái DB hiện tại là: {}",
                    info.getGhnOrderCode(), info.getStatus());
            return false;
        }

        boolean success = ghnService.cancelOrder(List.of(info.getGhnOrderCode()));
        if (success) {
            info.setStatus("cancel");
            shippingInfoRepository.save(info);
            log.info("[Shipping] Order {} cancelled successfully", info.getGhnOrderCode());
        }
        return success;
    }
}
