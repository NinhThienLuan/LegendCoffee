package fpt.legendcoffee.controller;

import fpt.legendcoffee.dto.request.PromotionRequestDTO;
import fpt.legendcoffee.dto.response.PromotionResponseDTO;
import fpt.legendcoffee.service.PromotionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
public class PromotionController {
    @Autowired
    private PromotionService promotionService;

    // CREATE
    @PostMapping("/promotion/create")
    public PromotionResponseDTO create(@RequestBody PromotionRequestDTO request) {
        return promotionService.create(request);
    }

    // UPDATE
    @PutMapping("/promotion/{id}")
    public PromotionResponseDTO update(@PathVariable Long id,
                                       @RequestBody PromotionRequestDTO request) {
        return promotionService.update(id, request);
    }

    // DELETE (cẩn thận xóa data gốc)
//    @DeleteMapping("/{id}")
//    public void delete(@PathVariable Long id) {
//        promotionService.delete(id);
//    }

    // GET ALL
    @GetMapping("/promotion")
    public List<PromotionResponseDTO> getAll() {
        return promotionService.getAll();
    }

    // GET BY ID
    @GetMapping("/promotion/{id}")
    public PromotionResponseDTO getById(@PathVariable Long id) {
        return promotionService.getById(id);
    }
}
