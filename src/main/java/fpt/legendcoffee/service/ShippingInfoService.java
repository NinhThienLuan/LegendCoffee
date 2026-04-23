package fpt.legendcoffee.service;

import fpt.legendcoffee.entity.ShippingInfo;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public interface ShippingInfoService {
    Optional<ShippingInfo> findByOrderId(Long orderId);
}
