package fpt.legendcoffee.service.serviceImpl;

import fpt.legendcoffee.dto.request.VoucherRequestDTO;
import fpt.legendcoffee.dto.response.VoucherResponseDTO;
import fpt.legendcoffee.entity.Voucher;
import fpt.legendcoffee.entity.enumeration.VoucherType;
import fpt.legendcoffee.repository.VoucherRepository;
import fpt.legendcoffee.service.VoucherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 *
 */
@Service
public class VoucherServiceImpl  implements VoucherService {
    @Autowired
    private VoucherRepository voucherRepository;


    @Override
    public List<VoucherResponseDTO> getVouchers() {
        List<Voucher> vouchers = voucherRepository.findAll();

        return vouchers.stream().map(voucher -> {
            VoucherResponseDTO response = new VoucherResponseDTO();
            response.setId(voucher.getId());
            response.setCode(voucher.getCode());
            response.setType(voucher.getType());
            response.setValue(voucher.getValue());
            response.setConditionMin(voucher.getConditionMin());
            response.setUsageLimit(voucher.getUsageLimit());
            response.setUsedCount(voucher.getUsedCount());
            response.setStartDate(voucher.getStartDate());
            response.setEndDate(voucher.getEndDate());
            response.setActive(true);
            return response;
        }).toList();
    }

    @Override
    public VoucherResponseDTO create(VoucherRequestDTO request) {

        // check code tồn tại
        if (voucherRepository.existsByCode(request.getCode())) {
            throw new RuntimeException("Code đã tồn tại");
        }

        // validate thời gian
        if (request.getEndDate().isBefore(request.getStartDate())) {
            throw new RuntimeException("End date phải sau start date");
        }

        // tạo entity
        Voucher voucher = new Voucher();
        voucher.setCode(request.getCode());
        voucher.setType(request.getType());
        voucher.setValue(request.getValue());
        voucher.setStartDate(request.getStartDate());
        voucher.setEndDate(request.getEndDate());
        voucher.setConditionMin(request.getConditionMin());
        voucher.setUsageLimit(request.getUsageLimit());
        voucher.setUsedCount(0);
        voucher.setActive(true);

        voucherRepository.save(voucher);

        // map sang response
        VoucherResponseDTO response = new VoucherResponseDTO();
        response.setId(voucher.getId());
        response.setCode(voucher.getCode());
        response.setType(voucher.getType());
        response.setValue(voucher.getValue());
        response.setConditionMin(voucher.getConditionMin());
        response.setUsageLimit(voucher.getUsageLimit());
        response.setUsedCount(voucher.getUsedCount());
        response.setStartDate(voucher.getStartDate());
        response.setEndDate(voucher.getEndDate());
        response.setActive(true);

        return response;
    }

    @Override
    public VoucherResponseDTO update(Long id, VoucherRequestDTO request) {
        Voucher voucher = voucherRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Voucher không tồn tại"));

        //check used
        if (voucher.getUsedCount() > 0) {
            throw new RuntimeException("Voucher đã được sử dụng, không thể cập nhật");
        }

        // validate thời gian
        if (request.getEndDate().isBefore(request.getStartDate())) {
            throw new RuntimeException("End date phải sau start date");
        }

        // update field
        voucher.setType(request.getType());
        voucher.setValue(request.getValue());
        voucher.setStartDate(request.getStartDate());
        voucher.setEndDate(request.getEndDate());
        voucher.setConditionMin(request.getConditionMin());
        voucher.setUsageLimit(request.getUsageLimit());

        voucherRepository.save(voucher);

        // map response
        VoucherResponseDTO response = new VoucherResponseDTO();
        response.setId(voucher.getId());
        response.setCode(voucher.getCode());
        response.setType(voucher.getType());
        response.setValue(voucher.getValue());
        response.setConditionMin(voucher.getConditionMin());
        response.setUsageLimit(voucher.getUsageLimit());
        response.setUsedCount(voucher.getUsedCount());
        response.setStartDate(voucher.getStartDate());
        response.setEndDate(voucher.getEndDate());
        response.setActive(voucher.getActive());

        return response;
    }

    @Override
    public VoucherResponseDTO getByCode(String code) {

        Voucher voucher = voucherRepository.findByCode(code)
                .orElseThrow(() -> new RuntimeException("Voucher không tồn tại"));

        VoucherResponseDTO response = new VoucherResponseDTO();
        response.setId(voucher.getId());
        response.setCode(voucher.getCode());
        response.setType(voucher.getType());
        response.setValue(voucher.getValue());
        response.setConditionMin(voucher.getConditionMin());
        response.setUsageLimit(voucher.getUsageLimit());
        response.setUsedCount(voucher.getUsedCount());
        response.setStartDate(voucher.getStartDate());
        response.setEndDate(voucher.getEndDate());
        response.setActive(voucher.getActive());

        return response;
    }

    @Override
    public void unActiveVoucher(Long id) {
        Voucher voucher = voucherRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Voucher không tồn tại"));

        voucher.setActive(false);

        voucherRepository.save(voucher);
    }


    @Override
    public BigDecimal applyVoucher(String code, BigDecimal orderTotal) {

        Voucher voucher = voucherRepository.findByCode(code)
                .orElseThrow(() -> new RuntimeException("Voucher không tồn tại"));



        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(voucher.getStartDate()) || now.isAfter(voucher.getEndDate())) {
            throw new RuntimeException("Voucher đã hết hạn hoặc chưa bắt đầu");
        }

        // check active
        if (!voucher.getActive()) {
            throw new RuntimeException("Voucher không hoạt động");
        }

        // check usage
        if (voucher.getUsedCount() >= voucher.getUsageLimit()) {
            throw new RuntimeException("Voucher đã hết lượt sử dụng");
        }

        // check min order
        if (orderTotal.compareTo(voucher.getConditionMin()) < 0) {
            throw new RuntimeException("Không đủ điều kiện áp dụng voucher");
        }

        // tính discount
        BigDecimal discount;

        if (voucher.getType() == VoucherType.PERCENT) {
            discount = orderTotal.multiply(voucher.getValue())
                    .divide(BigDecimal.valueOf(100));
        } else {
            discount = voucher.getValue();
        }

        return discount;
    }

    @Override
    public void increaseUsage(String code) {

        Voucher voucher = voucherRepository.findByCode(code)
                .orElseThrow(() -> new RuntimeException("Voucher không tồn tại"));

        voucher.setUsedCount(voucher.getUsedCount() + 1);

        voucherRepository.save(voucher);
    }
 }

