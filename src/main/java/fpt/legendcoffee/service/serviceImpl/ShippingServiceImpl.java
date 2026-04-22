package fpt.legendcoffee.service.serviceImpl;

import fpt.legendcoffee.common.exception.GHNException;
import fpt.legendcoffee.common.properties.GHNProperties;
import fpt.legendcoffee.dto.app.*;
import fpt.legendcoffee.dto.ghn.*;
import fpt.legendcoffee.entity.Order;
import fpt.legendcoffee.entity.ShippingInfo;
import fpt.legendcoffee.entity.enumeration.OrderStatus;
import fpt.legendcoffee.entity.enumeration.PaymentStatus;
import fpt.legendcoffee.repository.OrderRepository;
import fpt.legendcoffee.repository.PaymentRepository;
import fpt.legendcoffee.repository.ShippingInfoRepository;
import fpt.legendcoffee.service.GHNService;
import fpt.legendcoffee.service.ShippingService;
import fpt.legendcoffee.service.WalletService;
import fpt.legendcoffee.entity.Payment;
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

    private static final int DEFAULT_ITEM_WEIGHT_GRAMS = 250;
    private static final int MIN_WEIGHT_GRAMS = 50;
    private static final int DEFAULT_LENGTH_CM = 20;
    private static final int DEFAULT_WIDTH_CM = 20;
    private static final int DEFAULT_HEIGHT_CM = 10;
    private volatile boolean senderLocationValidated;
    private volatile String effectiveFromWardCode;
    private volatile String effectiveFromWardName;
    private volatile String effectiveFromDistrictName;
    private volatile String effectiveFromProvinceName;

    private final GHNService ghnService;
    private final ShippingInfoRepository shippingInfoRepository;
    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;
    private final WalletService walletService;
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
        validateSenderLocationConfig();
        int effectiveWeight = safeWeight(query.getWeight());

        // 1. Lấy danh sách dịch vụ khả dụng từ kho -> đến địa chỉ giao
        List<GHNServiceDTO> services = ghnService.getAvailableServices(
                props.getFromDistrictId(), query.getDistrictId());

        if (services == null || services.isEmpty()) {
            return List.of();
        }

        // 2. Gọi fee + leadtime song song cho từng dịch vụ (CompletableFuture)
        List<CompletableFuture<ShippingOptionDTO>> futures = services.stream()
                .filter(service -> service.getServiceTypeId() == null || service.getServiceTypeId() != 5)
                .map(service -> CompletableFuture
                        .supplyAsync(() -> buildShippingOption(service, query, effectiveWeight)))
                .toList();

        // 3. Chờ tất cả hoàn thành và gộp kết quả
        List<ShippingOptionDTO> options = new ArrayList<>();
        for (CompletableFuture<ShippingOptionDTO> future : futures) {
            try {
                ShippingOptionDTO opt = future.get();
                if (opt != null)
                    options.add(opt);
            } catch (Exception e) {
                log.warn("[Shipping] Failed to get option for one service: {}", e.getMessage());
            }
        }

        if (options.isEmpty()) {
            throw new GHNException(
                    "GHN không trả được phí vận chuyển cho địa chỉ này. Vui lòng kiểm tra cấu hình kho gửi hoặc thử địa chỉ khác.");
        }

        return options;
    }

    private void validateSenderLocationConfig() {
        if (senderLocationValidated) {
            return;
        }

        synchronized (this) {
            if (senderLocationValidated) {
                return;
            }

            if (props.getFromDistrictId() == null || isBlank(props.getFromWardCode())) {
                throw new GHNException("Thiếu cấu hình kho gửi GHN: from_district_id hoặc from_ward_code.");
            }

            List<WardDTO> wards = ghnService.getWards(props.getFromDistrictId());
            if (wards == null || wards.isEmpty()) {
                throw new GHNException("Không tải được danh sách phường/xã của kho gửi GHN.");
            }

            String configuredWardCode = props.getFromWardCode().trim();
            WardDTO matchedWard = wards.stream()
                    .filter(w -> w.getWardCode() != null
                            && w.getWardCode().trim().equalsIgnoreCase(configuredWardCode))
                    .findFirst()
                    .orElse(null);

            if (matchedWard != null) {
                effectiveFromWardCode = matchedWard.getWardCode().trim();
                effectiveFromWardName = matchedWard.getWardName();
            } else {
                WardDTO fallbackWard = wards.get(0);
                if (fallbackWard.getWardCode() == null || fallbackWard.getWardCode().trim().isEmpty()) {
                    throw new GHNException("Không tìm được from_ward_code hợp lệ cho from_district_id cấu hình.");
                }

                effectiveFromWardCode = fallbackWard.getWardCode().trim();
                effectiveFromWardName = fallbackWard.getWardName();
                log.warn("[Shipping] from_ward_code={} không thuộc from_district_id={}. Fallback sang wardCode={} ({})",
                        configuredWardCode,
                        props.getFromDistrictId(),
                        effectiveFromWardCode,
                        fallbackWard.getWardName() != null ? fallbackWard.getWardName() : "N/A");
            }

            resolveDistrictProvinceNames(props.getFromDistrictId());

            senderLocationValidated = true;
        }
    }

    private void resolveDistrictProvinceNames(Integer fromDistrictId) {
        List<ProvinceDTO> provinces = ghnService.getProvinces();
        if (provinces == null || provinces.isEmpty()) {
            throw new GHNException("Không lấy được danh sách tỉnh/thành GHN để xác định kho gửi.");
        }

        for (ProvinceDTO province : provinces) {
            if (province == null || province.getProvinceId() == null) {
                continue;
            }

            List<DistrictDTO> districts = ghnService.getDistricts(province.getProvinceId());
            if (districts == null || districts.isEmpty()) {
                continue;
            }

            for (DistrictDTO district : districts) {
                if (district != null && district.getDistrictId() != null
                        && district.getDistrictId().equals(fromDistrictId)) {
                    effectiveFromDistrictName = district.getDistrictName();
                    effectiveFromProvinceName = province.getProvinceName();
                    return;
                }
            }
        }

        throw new GHNException("Không tìm thấy tên quận/huyện và tỉnh/thành cho from_district_id trong GHN.");
    }

    private ShippingOptionDTO buildShippingOption(GHNServiceDTO service, AddressQueryDTO query, int effectiveWeight) {
        try {
            // Gọi fee
            FeeRequestDTO feeReq = FeeRequestDTO.builder()
                    .serviceId(service.getServiceId())
                    .serviceTypeId(service.getServiceTypeId())
                    .fromDistrictId(props.getFromDistrictId())
                    .fromWardCode(effectiveFromWardCode)
                    .toDistrictId(query.getDistrictId())
                    .toWardCode(query.getWardCode())
                    .weight(effectiveWeight)
                    .length(DEFAULT_LENGTH_CM)
                    .width(DEFAULT_WIDTH_CM)
                    .height(DEFAULT_HEIGHT_CM)
                    .insuranceValue(query.getInsuranceValue() != null ? query.getInsuranceValue() : 0L)
                    .build();

            // Gọi leadtime
            LeadtimeRequestDTO leadReq = LeadtimeRequestDTO.builder()
                    .serviceId(service.getServiceId())
                    .fromDistrictId(props.getFromDistrictId())
                    .fromWardCode(effectiveFromWardCode)
                    .toDistrictId(query.getDistrictId())
                    .toWardCode(query.getWardCode())
                    .build();

            // Gọi song song fee + leadtime cho service này
            CompletableFuture<FeeResponseDTO> feeFuture = CompletableFuture
                    .supplyAsync(() -> ghnService.calculateFee(feeReq));
            CompletableFuture<LeadtimeResponseDTO> leadFuture = CompletableFuture
                    .supplyAsync(() -> ghnService.getLeadtime(leadReq));

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
            log.warn("[Shipping] Skip serviceId={} due to invalid GHN response: {}",
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
    public ShippingInfo saveShippingInfo(CheckoutRequestDTO checkout, Order order) {
        log.info("[Shipping] Saving shipping info for order: {}", order.getId());

        // Tránh tạo thông tin vận chuyển trùng cho cùng một order
        if (shippingInfoRepository.existsByOrderId(order.getId())) {
            throw new GHNException("Đơn hàng này đã có thông tin vận chuyển");
        }

        ShippingInfo info = ShippingInfo.builder()
                .order(order)
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
                .shippingFee(checkout.getShippingFee())
                .paymentTypeId(checkout.getPaymentTypeId())
                .status("pending_payment") // Trạng thái chờ thanh toán
                .note(checkout.getNote())
                .build();

        return shippingInfoRepository.save(info);
    }

    @Override
    @Transactional
    public ShippingInfo pushOrderToGHN(Long orderId) {
        log.info("[Shipping] Pushing order {} to GHN gateway", orderId);

        ShippingInfo info = shippingInfoRepository.findByOrderId(orderId)
                .orElseThrow(() -> new GHNException("Không tìm thấy thông tin vận chuyển cho đơn hàng: " + orderId));

        if (info.getGhnOrderCode() != null) {
            log.warn("[Shipping] Order {} already has a GHN code: {}", orderId, info.getGhnOrderCode());
            return info;
        }

        // Tạo request cho GHN
        CreateOrderRequestDTO createReq = CreateOrderRequestDTO.builder()
                .toName(info.getRecipientName())
                .toPhone(info.getRecipientPhone())
                .toAddress(info.getRecipientAddress())
                .toWardName(info.getWardName())
                .toDistrictName(info.getDistrictName())
                .toProvinceName(info.getProvinceName())
                .toWardCode(info.getWardCode())
                .toDistrictId(info.getDistrictId())
                .serviceId(info.getServiceId())
                .serviceTypeId(2) // Bắt buộc truyền 2 (Giao chuẩn)
                .paymentTypeId(info.getPaymentTypeId())
                .weight(500) // TODO: tính từ giỏ hàng thực tế
                .length(20)
                .width(20)
                .height(10)
                .insuranceValue(0L)
                .codAmount(info.getPaymentTypeId() == 2 ? info.getShippingFee() : 0L)
                .note(info.getNote())
                .requiredNote("CHOTHUHANG")
                .items(List.of(
                        CreateOrderRequestDTO.OrderItemDTO.builder()
                                .name("Đơn hàng Legend Coffee")
                                .quantity(1)
                                .weight(500)
                                .build()))
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

        // Cập nhật thông tin vào DB
        info.setGhnOrderCode(ghnResponse.getOrderCode());
        info.setShippingFee(ghnResponse.getTotalFee());
        info.setStatus("ready_to_pick");
        info.setExpectedDeliveryTime(expectedTime);

        return shippingInfoRepository.save(info);
    }

    private int safeWeight(Integer rawWeight) {
        if (rawWeight == null) {
            return 500;
        }
        return Math.max(MIN_WEIGHT_GRAMS, rawWeight);
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private String firstNonBlank(String... values) {
        if (values == null) {
            return null;
        }

        for (String value : values) {
            if (!isBlank(value)) {
                return value.trim();
            }
        }
        return null;
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
        boolean isDelivering = finalStatus.contains("deliv") || finalStatus.contains("transport")
                || finalStatus.contains("picking");
        boolean isFinished = finalStatus.contains("delivered") || finalStatus.contains("received")
                || finalStatus.contains("finish");

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

        // Kiểm tra trạng thái trong DB: chỉ cho phép huỷ khi còn ở trạng thái
        // 'ready_to_pick'
        // Tránh phụ thuộc hoàn toàn vào API GHN nếu trạng thái trên hệ thống đối tác
        // chưa cập nhật kịp
        if (!"ready_to_pick".equalsIgnoreCase(info.getStatus())) {
            log.warn("[Shipping] Không thể huỷ đơn {} vì trạng thái DB hiện tại là: {}",
                    info.getGhnOrderCode(), info.getStatus());
            return false;
        }

        boolean success = ghnService.cancelOrder(List.of(info.getGhnOrderCode()));
        if (success) {
            info.setStatus("cancel");
            shippingInfoRepository.save(info);

            // Cập nhật trạng thái đơn hàng thành CANCELLED
            Order order = info.getOrder();
            order.setStatus(OrderStatus.CANCELLED);
            orderRepository.save(order);

            log.info("[Shipping] Order {} cancelled successfully and Order status updated to CANCELLED", info.getGhnOrderCode());

            // Xử lý hoàn tiền trực tiếp nếu đã thanh toán
            try {
                processRefund(order);
            } catch (Exception e) {
                log.error("[Shipping] Lỗi khi hoàn tiền cho đơn hàng #{}: {}", order.getId(), e.getMessage());
            }
        }
        return success;
    }

    private void processRefund(Order order) {
        // Tìm thanh toán thành công của đơn hàng
        java.util.Optional<Payment> paymentOpt = paymentRepository.findByOrderIdAndStatus(order.getId(), PaymentStatus.SUCCESS);
        
        if (paymentOpt.isPresent()) {
            Payment payment = paymentOpt.get();
            java.math.BigDecimal refundAmount = payment.getAmount();

            if (refundAmount != null && refundAmount.compareTo(java.math.BigDecimal.ZERO) > 0) {
                String adminDesc = "Hoàn tiền cho đơn hàng #" + order.getId() + " bị huỷ";
                String userDesc = "Hoàn tiền từ đơn hàng #" + order.getId();

                // 1. Trừ tiền admin
                walletService.debitAdminWallet(refundAmount, adminDesc);

                // 2. Cộng tiền cho user
                if (order.getUser() != null) {
                    walletService.creditWallet(order.getUser().getId(), refundAmount, userDesc);
                    log.info("[Shipping] Đã hoàn {} cho UserId={} từ đơn hàng #{}", refundAmount, order.getUser().getId(), order.getId());
                } else {
                    log.warn("[Shipping] Không tìm thấy User để hoàn tiền cho đơn hàng #{}", order.getId());
                }
            }
        } else {
            log.info("[Shipping] Đơn hàng #{} chưa có thanh toán thành công, không cần hoàn tiền", order.getId());
        }
    }
}
