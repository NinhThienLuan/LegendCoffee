package fpt.legendcoffee.service.serviceImpl;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fpt.legendcoffee.dto.request.ProductRequestDTO;
import fpt.legendcoffee.dto.request.ProductVariantRequestDTO;
import fpt.legendcoffee.dto.response.ProductResponseDTO;
import fpt.legendcoffee.entity.Category;
import fpt.legendcoffee.entity.Product;
import fpt.legendcoffee.entity.ProductVariant;
import fpt.legendcoffee.repository.CategoryRepository;
import fpt.legendcoffee.repository.ProductRepository;
import fpt.legendcoffee.repository.ProductVariantRepository;
import fpt.legendcoffee.service.ImageService;
import fpt.legendcoffee.service.ProductService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final ImageService imageService;
    private final CategoryRepository categoryRepository;
    private final ProductVariantRepository productVariantRepository;

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

        product = productRepository.save(product);

        // Lưu các biến thể
        if (request.getVariants() != null) {
            for (ProductVariantRequestDTO variantDTO : request.getVariants()) {
                ProductVariant variant = buildVariant(variantDTO, product);
                productVariantRepository.save(variant);
            }
        }

        return product;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductResponseDTO> getAllProductResponses() {
        List<Product> products = productRepository.findAll();
        return products.stream().map(product -> {
            List<ProductVariant> variants = productVariantRepository.findByProduct(product);

            BigDecimal minPrice = null;
            int totalStock = 0;

            if (!variants.isEmpty()) {
                minPrice = variants.stream()
                        .map(ProductVariant::getPrice)
                        .filter(p -> p != null)
                        .min(java.math.BigDecimal::compareTo)
                        .orElse(null);

                totalStock = variants.stream()
                        .mapToInt(v -> v.getStockQuantity() != null ? v.getStockQuantity() : 0)
                        .sum();
            }

            return ProductResponseDTO.builder()
                    .id(product.getId())
                    .name(product.getName())
                    .categoryName(product.getCategory() != null ? product.getCategory().getCategoryName() : null)
                    .price(minPrice)
                    .stockQuantity(totalStock)
                    .soldCount(0) // Default 0 as requested
                    .isActive(product.getIsActive())
                    .imageUrl(product.getImageUrl())
                    .build();
        }).toList();
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

        product = productRepository.save(product);

        // Xóa biến thể cũ và thêm lại từ form
        productVariantRepository.deleteByProduct(product);
        if (request.getVariants() != null) {
            for (ProductVariantRequestDTO variantDTO : request.getVariants()) {
                ProductVariant variant = buildVariant(variantDTO, product);
                productVariantRepository.save(variant);
            }
        }

        return product;
    }

    @Override
    @Transactional
    public void deleteProduct(Long id) {
        Product product = getProductById(id);
        // Xóa biến thể trước khi xóa sản phẩm
        productVariantRepository.deleteByProduct(product);
        if (product.getImagePublicId() != null && !product.getImagePublicId().isBlank()) {
            imageService.delete(product.getImagePublicId());
        }
        productRepository.delete(product);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private ProductVariant buildVariant(ProductVariantRequestDTO dto, Product product) {
        return ProductVariant.builder()
                .product(product)
                .variantName(dto.getVariantName())
                .packaging(dto.getPackaging())
                .size(dto.getSize())
                .price(dto.getPrice())
                .stockQuantity(dto.getStockQuantity())
                .isActive(dto.getIsActive() != null ? dto.getIsActive() : Boolean.TRUE)
                .build();
    }

    private void validateDateRange(ProductRequestDTO request) {
        if (request.getManufacturerDate() != null
                && request.getExpiryDate() != null
                && !request.getExpiryDate().isAfter(request.getManufacturerDate())) {
            throw new IllegalArgumentException("Ngày hết hạn phải sau ngày sản xuất");
        }
    }
}