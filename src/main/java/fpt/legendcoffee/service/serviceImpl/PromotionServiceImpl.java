package fpt.legendcoffee.service.serviceImpl;

import fpt.legendcoffee.dto.request.PromotionRequestDTO;
import fpt.legendcoffee.dto.response.PromotionResponseDTO;
import fpt.legendcoffee.entity.Promotion;
import fpt.legendcoffee.repository.PromotionRepository;
import fpt.legendcoffee.service.PromotionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PromotionServiceImpl implements PromotionService {
    @Autowired
    private PromotionRepository promotionRepository;

    @Override
    public PromotionResponseDTO create(PromotionRequestDTO request) {
        if (request.getEndDate().isBefore(request.getStartDate())) {
            throw new RuntimeException("End date phải sau start date");
        }

        Promotion p = new Promotion();
        p.setType(request.getType());
        p.setValue(request.getValue());
        p.setStartDate(request.getStartDate());
        p.setEndDate(request.getEndDate());

        promotionRepository.save(p);

        PromotionResponseDTO res = new PromotionResponseDTO();
        res.setId(p.getId());
        res.setType(p.getType());
        res.setValue(p.getValue());
        res.setStartDate(p.getStartDate());
        res.setEndDate(p.getEndDate());

        return res;
    }

    @Override
    public PromotionResponseDTO update(Long id, PromotionRequestDTO request) {
        Promotion p = promotionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Promotion không tồn tại"));

        // validate thời gian
        if (request.getEndDate().isBefore(request.getStartDate())) {
            throw new RuntimeException("End date phải sau start date");
        }

        // update
        p.setType(request.getType());
        p.setValue(request.getValue());
        p.setStartDate(request.getStartDate());
        p.setEndDate(request.getEndDate());

        promotionRepository.save(p);

        // response
        PromotionResponseDTO res = new PromotionResponseDTO();
        res.setId(p.getId());
        res.setType(p.getType());
        res.setValue(p.getValue());
        res.setStartDate(p.getStartDate());
        res.setEndDate(p.getEndDate());

        return res;
    }

    @Override
    public void delete(Long id) {
        Promotion p = promotionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Promotion không tồn tại"));

        promotionRepository.delete(p);
    }

    @Override
    public PromotionResponseDTO getById(Long id) {
        Promotion p = promotionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Promotion không tồn tại"));

        PromotionResponseDTO res = new PromotionResponseDTO();
        res.setId(p.getId());
        res.setType(p.getType());
        res.setValue(p.getValue());
        res.setStartDate(p.getStartDate());
        res.setEndDate(p.getEndDate());

        return res;
    }

    @Override
    public List<PromotionResponseDTO> getAll() {
        List<Promotion> promotions = promotionRepository.findAll();

        return promotions.stream().map(p -> {
            PromotionResponseDTO res = new PromotionResponseDTO();
            res.setId(p.getId());
            res.setType(p.getType());
            res.setValue(p.getValue());
            res.setStartDate(p.getStartDate());
            res.setEndDate(p.getEndDate());
            return res;
        }).toList();
    }

}

