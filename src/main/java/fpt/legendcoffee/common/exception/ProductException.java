package fpt.legendcoffee.common.exception;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

@ControllerAdvice
public class ProductException {
    // 1. Xử lý lỗi file quá lớn (Cấu hình hệ thống)
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public String handleMaxUploadSizeExceeded(MaxUploadSizeExceededException ex, jakarta.servlet.http.HttpServletRequest request, jakarta.servlet.http.HttpServletResponse response) {
        org.springframework.web.servlet.FlashMap flashMap = org.springframework.web.servlet.support.RequestContextUtils.getOutputFlashMap(request);
        if (flashMap != null) {
            flashMap.put("error", "Dung lượng file tải lên quá lớn. Vui lòng chọn file nhỏ hơn (Tối đa 5MB)!");
            org.springframework.web.servlet.FlashMapManager flashMapManager = org.springframework.web.servlet.support.RequestContextUtils.getFlashMapManager(request);
            if (flashMapManager != null) {
                flashMapManager.saveOutputFlashMap(flashMap, request, response);
            }
        }
        String referer = request.getHeader("Referer");
        if (referer != null && !referer.isEmpty()) {
            return "redirect:" + referer;
        }
        return "redirect:/products";
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public String handleValidationException(IllegalArgumentException ex, jakarta.servlet.http.HttpServletRequest request, jakarta.servlet.http.HttpServletResponse response) {
        org.springframework.web.servlet.FlashMap flashMap = org.springframework.web.servlet.support.RequestContextUtils.getOutputFlashMap(request);
        if (flashMap != null) {
            flashMap.put("error", ex.getMessage());
            org.springframework.web.servlet.FlashMapManager flashMapManager = org.springframework.web.servlet.support.RequestContextUtils.getFlashMapManager(request);
            if (flashMapManager != null) {
                flashMapManager.saveOutputFlashMap(flashMap, request, response);
            }
        }
        String referer = request.getHeader("Referer");
        if (referer != null && !referer.isEmpty()) {
            return "redirect:" + referer;
        }
        return "redirect:/";
    }
}
