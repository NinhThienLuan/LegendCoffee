package fpt.legendcoffee.service.serviceImpl;

import fpt.legendcoffee.dto.request.ComboItemRequestDTO;
import fpt.legendcoffee.dto.request.ComboRequestDTO;
import fpt.legendcoffee.dto.response.ComboItemResponseDTO;
import fpt.legendcoffee.dto.response.ComboResponseDTO;
import fpt.legendcoffee.entity.Combo;
import fpt.legendcoffee.entity.ComboItem;
import fpt.legendcoffee.entity.ProductVariant;
import fpt.legendcoffee.repository.ComboItemRepository;
import fpt.legendcoffee.repository.ComboRepository;
import fpt.legendcoffee.repository.ProductVariantRepository;
import fpt.legendcoffee.service.ComboService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class ComboServiceImpl implements ComboService {

    private final ComboRepository comboRepository;
    private final ComboItemRepository comboItemRepository;
    private final ProductVariantRepository productVariantRepository;

    @Override
    @Transactional(readOnly = true)
    public List<ComboResponseDTO> getAllCombos() {
        return comboRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ComboResponseDTO> getActiveCombos() {
        return comboRepository.findAll().stream()
                .filter(this::isCurrentlyAvailable)
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ComboResponseDTO getById(Long id) {
        return toResponse(getComboOrThrow(id));
    }

    @Override
    @Transactional
    public ComboResponseDTO create(ComboRequestDTO request) {
        request.validate();
        validateComboName(null, request.getName());
        BigDecimal originalPrice = validateAndCalculateOriginalPrice(request.getItems());
        validatePromisingPrice(request.getPrice(), originalPrice);

        Combo combo = Combo.builder()
                .name(request.getName())
                .description(request.getDescription())
                .price(request.getPrice())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .isActive(request.getIsActive() != null ? request.getIsActive() : Boolean.TRUE)
                .build();

        combo = comboRepository.save(combo);
        saveItems(combo, request.getItems());
        return toResponse(combo);
    }

    @Override
    @Transactional
    public ComboResponseDTO update(Long id, ComboRequestDTO request) {
        request.validate();
        Combo combo = getComboOrThrow(id);
        validateComboName(id, request.getName());
        BigDecimal originalPrice = validateAndCalculateOriginalPrice(request.getItems());
        validatePromisingPrice(request.getPrice(), originalPrice);

        combo.setName(request.getName());
        combo.setDescription(request.getDescription());
        combo.setPrice(request.getPrice());
        combo.setStartDate(request.getStartDate());
        combo.setEndDate(request.getEndDate());
        combo.setIsActive(request.getIsActive() != null ? request.getIsActive() : combo.getIsActive());
        combo = comboRepository.save(combo);

        comboItemRepository.deleteByComboId(combo.getId());
        saveItems(combo, request.getItems());
        return toResponse(combo);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Combo combo = getComboOrThrow(id);
        comboItemRepository.deleteByComboId(combo.getId());
        comboRepository.delete(combo);
    }

    private Combo getComboOrThrow(Long id) {
        return comboRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy combo với ID: " + id));
    }

    private void validateComboName(Long currentId, String name) {
        comboRepository.findByNameIgnoreCase(name).ifPresent(existing -> {
            if (!existing.getId().equals(currentId)) {
                throw new IllegalArgumentException("Tên combo đã tồn tại");
            }
        });
    }

    private BigDecimal validateAndCalculateOriginalPrice(List<ComboItemRequestDTO> items) {
        Set<Long> variantIds = new HashSet<>();
        BigDecimal originalPrice = BigDecimal.ZERO;

        for (ComboItemRequestDTO item : items) {
            if (!variantIds.add(item.getVariantId())) {
                throw new IllegalArgumentException("Combo không được chứa biến thể trùng nhau");
            }

            ProductVariant variant = productVariantRepository.findById(item.getVariantId())
                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy biến thể với ID: " + item.getVariantId()));

            if (!Boolean.TRUE.equals(variant.getIsActive())) {
                throw new IllegalArgumentException("Biến thể " + variant.getId() + " đang không hoạt động");
            }

            if (variant.getPrice() == null) {
                throw new IllegalArgumentException("Biến thể " + variant.getId() + " chưa có giá bán");
            }

            originalPrice = originalPrice.add(variant.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
        }

        return originalPrice;
    }

    private void validatePromisingPrice(BigDecimal comboPrice, BigDecimal originalPrice) {
        if (comboPrice == null || comboPrice.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Giá combo phải lớn hơn 0");
        }
        if (comboPrice.compareTo(originalPrice) >= 0) {
            throw new IllegalArgumentException("Giá combo phải thấp hơn tổng giá bán lẻ của các sản phẩm trong combo");
        }
    }

    private void saveItems(Combo combo, List<ComboItemRequestDTO> items) {
        for (ComboItemRequestDTO item : items) {
            ProductVariant variant = productVariantRepository.findById(item.getVariantId())
                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy biến thể với ID: " + item.getVariantId()));

            ComboItem comboItem = ComboItem.builder()
                    .combo(combo)
                    .variant(variant)
                    .quantity(item.getQuantity())
                    .build();
            comboItemRepository.save(comboItem);
        }
    }

    private ComboResponseDTO toResponse(Combo combo) {
        List<ComboItem> comboItems = comboItemRepository.findByComboId(combo.getId());
        List<ComboItemResponseDTO> itemResponses = comboItems.stream().map(item -> {
            BigDecimal unitPrice = item.getVariant() != null ? item.getVariant().getPrice() : BigDecimal.ZERO;
            BigDecimal lineTotal = unitPrice.multiply(BigDecimal.valueOf(item.getQuantity() == null ? 0 : item.getQuantity()));
            String itemImageUrl = null;
            if (item.getVariant() != null) {
                itemImageUrl = item.getVariant().getImageUrl();
                if ((itemImageUrl == null || itemImageUrl.isBlank()) && item.getVariant().getProduct() != null) {
                    itemImageUrl = item.getVariant().getProduct().getImageUrl();
                }
            }
            ComboItemResponseDTO dto = new ComboItemResponseDTO();
            dto.setId(item.getId());
            dto.setVariantId(item.getVariant() != null ? item.getVariant().getId() : null);
            dto.setVariantName(item.getVariant() != null ? item.getVariant().getVariantName() : null);
            dto.setProductName(item.getVariant() != null && item.getVariant().getProduct() != null ? item.getVariant().getProduct().getName() : null);
            dto.setImageUrl(itemImageUrl);
            dto.setQuantity(item.getQuantity());
            dto.setUnitPrice(unitPrice);
            dto.setLineTotal(lineTotal);
            return dto;
        }).toList();

        BigDecimal originalPrice = itemResponses.stream()
                .map(ComboItemResponseDTO::getLineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal savings = originalPrice.subtract(combo.getPrice() != null ? combo.getPrice() : BigDecimal.ZERO);

        // Use first item's image as the combo cover image
        String comboImageUrl = itemResponses.stream()
                .filter(i -> i.getImageUrl() != null && !i.getImageUrl().isBlank())
                .map(ComboItemResponseDTO::getImageUrl)
                .findFirst().orElse(null);

        ComboResponseDTO response = new ComboResponseDTO();
        response.setId(combo.getId());
        response.setName(combo.getName());
        response.setDescription(combo.getDescription());
        response.setPrice(combo.getPrice());
        response.setOriginalPrice(originalPrice);
        response.setSavings(savings);
        response.setImageUrl(comboImageUrl);
        response.setStartDate(combo.getStartDate());
        response.setEndDate(combo.getEndDate());
        response.setActive(combo.getIsActive());
        response.setAvailableNow(isCurrentlyAvailable(combo));
        response.setItems(itemResponses);
        return response;
    }

    private boolean isCurrentlyAvailable(Combo combo) {
        LocalDateTime now = LocalDateTime.now();
        return Boolean.TRUE.equals(combo.getIsActive())
                && combo.getStartDate() != null
                && combo.getEndDate() != null
                && !now.isBefore(combo.getStartDate())
                && !now.isAfter(combo.getEndDate());
    }
}

