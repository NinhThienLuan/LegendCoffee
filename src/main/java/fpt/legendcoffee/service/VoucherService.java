package fpt.legendcoffee.service;

import fpt.legendcoffee.dto.request.VoucherRequestDTO;
import fpt.legendcoffee.dto.response.VoucherResponseDTO;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public interface VoucherService {
    List<VoucherResponseDTO> getVouchers();

    VoucherResponseDTO create(VoucherRequestDTO request);

    VoucherResponseDTO update(Long id, VoucherRequestDTO request);

    VoucherResponseDTO getByCode(String code);

    void unActiveVoucher(Long id);

    //tính CHECKOUT ở order
    BigDecimal applyVoucher(String code, BigDecimal orderTotal);

    //CẬP NHẬT DB sau khi order thành công
    void increaseUsage(String code);
}
