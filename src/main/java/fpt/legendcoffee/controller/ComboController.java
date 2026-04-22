package fpt.legendcoffee.controller;

import fpt.legendcoffee.dto.request.ComboItemRequestDTO;
import fpt.legendcoffee.dto.request.ComboRequestDTO;
import fpt.legendcoffee.dto.response.ComboResponseDTO;
import fpt.legendcoffee.service.ComboService;
import fpt.legendcoffee.service.ProductVariantService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class ComboController {

    private final ComboService comboService;
    private final ProductVariantService productVariantService;

    @GetMapping("/combos")
    public String publicComboPage(Model model) {
        model.addAttribute("combos", comboService.getActiveCombos());
        model.addAttribute("pageType", "combos");
        return "product/catalogs";
    }

    @GetMapping("/combos/{id}")
    public String publicComboDetail(@PathVariable Long id, Model model) {
        model.addAttribute("combo", comboService.getById(id));
        return "product/combo-detail";
    }

    @GetMapping("/admin/combos")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public String adminComboList(Model model) {
        model.addAttribute("combos", comboService.getAllCombos());
        return "admin/combos";
    }

    @GetMapping("/admin/combos/new")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public String newComboForm(Model model) {
        prepareFormModel(model, new ComboRequestDTO(), false, null);
        return "admin/combo-form";
    }

    @PostMapping("/admin/combos")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public String createCombo(@Valid @ModelAttribute("request") ComboRequestDTO request,
                              BindingResult bindingResult,
                              Model model,
                              RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            prepareFormModel(model, request, false, null);
            model.addAttribute("error", "Vui lòng kiểm tra lại dữ liệu combo.");
            return "admin/combo-form";
        }

        try {
            comboService.create(request);
            redirectAttributes.addFlashAttribute("success", "Thêm combo thành công!");
            return "redirect:/admin/combos";
        } catch (IllegalArgumentException ex) {
            prepareFormModel(model, request, false, null);
            model.addAttribute("error", ex.getMessage());
            return "admin/combo-form";
        }
    }

    @GetMapping("/admin/combos/{id}/edit")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public String editComboForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            ComboResponseDTO response = comboService.getById(id);
            ComboRequestDTO request = toRequest(response);
            prepareFormModel(model, request, true, id);
            return "admin/combo-form";
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
            return "redirect:/admin/combos";
        }
    }

    @PostMapping("/admin/combos/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public String updateCombo(@PathVariable Long id,
                              @Valid @ModelAttribute("request") ComboRequestDTO request,
                              BindingResult bindingResult,
                              Model model,
                              RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            prepareFormModel(model, request, true, id);
            model.addAttribute("error", "Vui lòng kiểm tra lại dữ liệu combo.");
            return "admin/combo-form";
        }

        try {
            comboService.update(id, request);
            redirectAttributes.addFlashAttribute("success", "Cập nhật combo thành công!");
            return "redirect:/admin/combos";
        } catch (IllegalArgumentException ex) {
            prepareFormModel(model, request, true, id);
            model.addAttribute("error", ex.getMessage());
            return "admin/combo-form";
        }
    }

    @PostMapping("/admin/combos/{id}/delete")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public String deleteCombo(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            comboService.delete(id);
            redirectAttributes.addFlashAttribute("success", "Xóa combo thành công!");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/admin/combos";
    }

    private void prepareFormModel(Model model, ComboRequestDTO request, boolean editMode, Long comboId) {
        if (request.getItems() == null) {
            request.setItems(new ArrayList<>());
        }
        if (request.getItems().isEmpty()) {
            request.getItems().add(new ComboItemRequestDTO());
        }

        model.addAttribute("request", request);
        model.addAttribute("variants", productVariantService.getAllVariants());
        model.addAttribute("editMode", editMode);
        model.addAttribute("comboId", comboId);
    }

    private ComboRequestDTO toRequest(ComboResponseDTO response) {
        ComboRequestDTO request = new ComboRequestDTO();
        request.setName(response.getName());
        request.setDescription(response.getDescription());
        request.setPrice(response.getPrice());
        request.setStartDate(response.getStartDate());
        request.setEndDate(response.getEndDate());
        request.setIsActive(response.getActive());
        List<ComboItemRequestDTO> items = response.getItems().stream()
                .map(item -> new ComboItemRequestDTO(item.getVariantId(), item.getQuantity()))
                .toList();
        request.setItems(new ArrayList<>(items));
        return request;
    }
}

