package fpt.legendcoffee.service.serviceImpl;

import fpt.legendcoffee.entity.ShippingInfo;
import fpt.legendcoffee.repository.ShippingInfoRepository;
import fpt.legendcoffee.service.ShippingInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ShippingInfoServiceImpl implements ShippingInfoService {

    @Autowired
    private ShippingInfoRepository shippingInfoRepository;

    @Override
    public Optional<ShippingInfo> findByOrderId(Long orderId) {
        return shippingInfoRepository.findByOrderId(orderId);
    }
}
