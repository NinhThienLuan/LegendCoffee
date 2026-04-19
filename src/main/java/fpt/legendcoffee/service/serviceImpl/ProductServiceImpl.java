package fpt.legendcoffee.service.serviceImpl;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fpt.legendcoffee.dto.request.ProductRequestDTO;
import fpt.legendcoffee.entity.Category;
import fpt.legendcoffee.entity.Product;
import fpt.legendcoffee.repository.ProductRepository;
import fpt.legendcoffee.service.ImageService;
import fpt.legendcoffee.service.ProductService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final ImageService imageService;

    @Override
    @Transactional
    public Product addProduct(ProductRequestDTO request) {
        validateDateRange(request);

        String imageUrl = null;
        String imagePublicId = null;
        if (request.getImage() != null && !request.getImage().isEmpty()) {
            var uploadResult = imageService.upload(request.getImage());
            imageUrl = (String) uploadResult.get("secure_url");
            imagePublicId = (String) uploadResult.get("public_id");
        }

        Product product = Product.builder()
                .name(request.getName())
                .description(request.getDescription())
                .origin(request.getOrigin())
                .manufacturerDate(request.getManufacturerDate())
                .expiryDate(request.getExpiryDate())
                .isActive(request.getIsActive() != null ? request.getIsActive() : Boolean.TRUE)
                .imageUrl(imageUrl)
                .imagePublicId(imagePublicId)
                .build();

        if (request.getCategoryId() != null) {
            Category category = new Category();
            category.setId(request.getCategoryId());
            product.setCategory(category);
        }

        return productRepository.save(product);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Product getProductById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy sản phẩm với ID: " + id));
    }

    @Override
    @Transactional
    public Product updateProduct(Long id, ProductRequestDTO request) {
        validateDateRange(request);

        Product product = getProductById(id);

        if (request.getImage() != null && !request.getImage().isEmpty()) {
            var uploadResult = imageService.upload(request.getImage());
            String newImageUrl = (String) uploadResult.get("secure_url");
            String newImagePublicId = (String) uploadResult.get("public_id");

            if (product.getImagePublicId() != null && !product.getImagePublicId().isBlank()) {
                imageService.delete(product.getImagePublicId());
            }

            product.setImageUrl(newImageUrl);
            product.setImagePublicId(newImagePublicId);
        }

        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setOrigin(request.getOrigin());
        product.setManufacturerDate(request.getManufacturerDate());
        product.setExpiryDate(request.getExpiryDate());
        if (request.getIsActive() != null) {
            product.setIsActive(request.getIsActive());
        }

        if (request.getCategoryId() != null) {
            Category category = new Category();
            category.setId(request.getCategoryId());
            product.setCategory(category);
        } else {
            product.setCategory(null);
        }

        return productRepository.save(product);
    }

    @Override
    @Transactional
    public void deleteProduct(Long id) {
        Product product = getProductById(id);
        if (product.getImagePublicId() != null && !product.getImagePublicId().isBlank()) {
            imageService.delete(product.getImagePublicId());
        }
        productRepository.delete(product);
    }

    private void validateDateRange(ProductRequestDTO request) {
        LocalDate today = LocalDate.now();
        LocalDate manufacturerDate = request.getManufacturerDate();
        LocalDate expiryDate = request.getExpiryDate();

        if (manufacturerDate != null && manufacturerDate.isAfter(today)) {
            throw new IllegalArgumentException("Ngày sản xuất không được ở tương lai");
        }
        if (expiryDate != null && expiryDate.isBefore(today)) {
            throw new IllegalArgumentException("Ngày hết hạn phải ở tương lai");
        }
        if (manufacturerDate == null || expiryDate == null) {
            return;
        }
        if (!expiryDate.isAfter(manufacturerDate)) {
            throw new IllegalArgumentException("Ngày hết hạn phải sau ngày sản xuất");
        }
    }
    @Override
@Transactional(readOnly = true)
public ProductRequestDTO getProductRequestById(Long id) {
    Product product = getProductById(id);
    return ProductRequestDTO.builder()
            .name(product.getName())
            .description(product.getDescription())
            .origin(product.getOrigin())
            .manufacturerDate(product.getManufacturerDate())
            .expiryDate(product.getExpiryDate())
            .categoryId(product.getCategory() != null ? product.getCategory().getId() : null)
            .isActive(product.getIsActive())
            .build();
}

@Override
public ProductRequestDTO sanitize(ProductRequestDTO request) {
    return ProductRequestDTO.builder()
            .name(request.getName())
            .description(request.getDescription())
            .origin(request.getOrigin())
            .manufacturerDate(request.getManufacturerDate())
            .expiryDate(request.getExpiryDate())
            .categoryId(request.getCategoryId())
            .isActive(request.getIsActive())
            .build();
}
}