package fpt.legendcoffee.service;

import fpt.legendcoffee.dto.request.ComboRequestDTO;
import fpt.legendcoffee.dto.response.ComboResponseDTO;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface ComboService {
    List<ComboResponseDTO> getAllCombos();

    List<ComboResponseDTO> getActiveCombos();

    ComboResponseDTO getById(Long id);

    ComboResponseDTO create(ComboRequestDTO request);

    ComboResponseDTO update(Long id, ComboRequestDTO request);

    void delete(Long id);
}

