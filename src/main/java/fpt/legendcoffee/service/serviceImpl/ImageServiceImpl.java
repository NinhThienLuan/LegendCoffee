package fpt.legendcoffee.service.serviceImpl;

import java.io.IOException;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;

import fpt.legendcoffee.service.ImageService;

@Service
public class ImageServiceImpl implements ImageService {

    @Autowired
    private Cloudinary cloudinary;

    @Override
    @SuppressWarnings("unchecked")
        public Map<String, Object> upload(MultipartFile file) {
        try {
            // Sử dụng ObjectUtils của Cloudinary để tạo các tùy chọn (options)
            Map<String, Object> params = (Map<String, Object>) ObjectUtils.asMap(
                    "folder", "my_app_uploads", // Lưu vào folder cụ thể trên Cloudinary
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
        try {
            return (Map<String, Object>) cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
        } catch (IOException e) {
            throw new RuntimeException("Lỗi khi xóa file trên Cloudinary: " + e.getMessage());
        }
    }
}
