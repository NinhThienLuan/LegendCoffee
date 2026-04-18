package fpt.legendcoffee.service.serviceImpl;

import java.io.IOException;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;

import fpt.legendcoffee.service.ImageService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ImageServiceImpl implements ImageService {

    private final Cloudinary cloudinary;

    @Override
    @SuppressWarnings("unchecked")
    public Map<String, Object> upload(MultipartFile file) {
        try {
            // Sử dụng ObjectUtils của Cloudinary để tạo các tùy chọn (options)
            Map<String, Object> params = (Map<String, Object>) ObjectUtils.asMap(
                    "folder", "Legend_Coffee/Products", // Lưu vào folder cụ thể trên Cloudinary
                    "resource_type", "auto"      // Tự động nhận diện ảnh/video/raw
            );

            return (Map<String, Object>) cloudinary.uploader().upload(file.getBytes(), params);
        } catch (IOException e) {
            throw new RuntimeException("Lỗi trong quá trình upload file: " + e.getMessage());
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public Map<String, Object> delete(String publicId) {
        if (publicId == null || publicId.isBlank()) {
            return Map.of("result", "skipped");
        }

        try {
            return (Map<String, Object>) cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
        } catch (IOException e) {
            String message = e.getMessage() == null ? "" : e.getMessage().toLowerCase();
            if (message.contains("not found") || message.contains("404")) {
                return Map.of("result", "not_found", "public_id", publicId);
            }
            throw new RuntimeException("Lỗi khi xóa file trên Cloudinary: " + e.getMessage());
        }
    }
}
