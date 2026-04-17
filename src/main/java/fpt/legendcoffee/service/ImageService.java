package fpt.legendcoffee.service;

import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

public interface ImageService {
    // Trả về Map chứa toàn bộ thông tin từ Cloudinary (URL, Public ID, size,...)
    Map<String, Object> upload(MultipartFile file);

    // Bạn có thể thêm phương thức xóa ảnh nếu cần
    Map<String, Object> delete(String publicId);
}