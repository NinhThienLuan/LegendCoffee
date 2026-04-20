package fpt.legendcoffee.common.exception;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
@ControllerAdvice
public class ImageException {
// 1. Xử lý lỗi file quá lớn (Cấu hình hệ thống)
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public String handleMaxUploadSizeExceeded(RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("error", "Dung lượng file vượt quá giới hạn cho phép (Tối đa 5MB)!");
        return "redirect:/products/form-add"; 
    }

   

}
