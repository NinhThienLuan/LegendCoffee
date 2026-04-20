package fpt.legendcoffee.controller;

import fpt.legendcoffee.dto.request.VoucherRequestDTO;
import fpt.legendcoffee.dto.response.VoucherResponseDTO;
import fpt.legendcoffee.service.VoucherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
public class VoucherController {
    @Autowired
    private VoucherService voucherService;

    @GetMapping("/vouchers")
    public List<VoucherResponseDTO> getVouchers() {
        return voucherService.getVouchers();
    }

    @PostMapping("/voucher/create")
    public VoucherResponseDTO create(VoucherRequestDTO request) {
        return voucherService.create(request);
    }

    @PutMapping("/voucher/{id}")
    public VoucherResponseDTO update(@PathVariable Long id,
                                     @RequestBody VoucherRequestDTO request) {
        return voucherService.update(id, request);
    }

    @GetMapping("/voucher/{code}")
    public VoucherResponseDTO getByCode(@PathVariable String code) {
        return voucherService.getByCode(code);
    }

    @PutMapping("/voucher/{id}/unactive")
    public void unActive(@PathVariable Long id) {
        voucherService.unActiveVoucher(id);
    }
}
