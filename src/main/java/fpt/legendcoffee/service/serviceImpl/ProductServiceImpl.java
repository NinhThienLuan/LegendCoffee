package fpt.legendcoffee.service.serviceImpl;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fpt.legendcoffee.dto.request.ProductRequestDTO;
import fpt.legendcoffee.dto.request.ProductVariantRequestDTO;
import fpt.legendcoffee.dto.response.ProductDetailDTO;
import fpt.legendcoffee.dto.response.ProductResponseDTO;
import fpt.legendcoffee.entity.Category;
import fpt.legendcoffee.entity.Product;
import fpt.legendcoffee.entity.ProductVariant;
import fpt.legendcoffee.repository.CategoryRepository;
import fpt.legendcoffee.repository.ProductRepository;
import fpt.legendcoffee.repository.ProductVariantRepository;
import fpt.legendcoffee.repository.OrderItemRepository;
import fpt.legendcoffee.service.ImageService;
import fpt.legendcoffee.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final ImageService imageService;
    private final CategoryRepository categoryRepository;
    private final ProductVariantRepository productVariantRepository;
    private final OrderItemRepository orderItemRepository;

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
            Long defaultVariantId = null;
            String defaultVariantName = null;

            if (!variants.isEmpty()) {
                ProductVariant firstActive = variants.stream()
                        .filter(v -> Boolean.TRUE.equals(v.getIsActive()))
                        .findFirst()
                        .orElse(variants.get(0));

                defaultVariantId = firstActive.getId();
                defaultVariantName = firstActive.getVariantName();

                minPrice = variants.stream()
                        .map(ProductVariant::getPrice)
                        .filter(p -> p != null)
                        .min(BigDecimal::compareTo)
                        .orElse(null);

                totalStock = variants.stream()
                        .mapToInt(v -> v.getStockQuantity() != null ? v.getStockQuantity() : 0)
                        .sum();
            }

            int soldCount = 0;
            try {
                List<String> successStatuses = List.of("CONFIRMED", "SHIPPING", "COMPLETED");
                Integer totalSold = orderItemRepository.sumQuantityByProductId(product.getId(), successStatuses);
                soldCount = totalSold != null ? totalSold : 0;
            } catch (Exception e) {
                log.warn("Failed to calculate sold count for product {}: {}", product.getId(), e.getMessage());
            }

            return ProductResponseDTO.builder()
                    .id(product.getId())
                    .name(product.getName())
                    .description(product.getDescription())
                    .categoryName(product.getCategory() != null ? product.getCategory().getCategoryName() : null)
                    .price(minPrice)
                    .stockQuantity(totalStock)
                    .soldCount(soldCount)
                    .isActive(product.getIsActive())
                    .imageUrl(product.getImageUrl())
                    .defaultVariantId(defaultVariantId)
                    .defaultVariantName(defaultVariantName)
                    .build();
        }).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductResponseDTO> searchProducts(String keyword, Boolean active) {
        List<ProductResponseDTO> results = getAllProductResponses();

        if (keyword != null && !keyword.isBlank()) {
            final String kw = keyword.trim().toLowerCase();
            results = results.stream()
                    .filter(p -> p.getName() != null && p.getName().toLowerCase().contains(kw))
                    .toList();
        }

        if (active != null) {
            results = results.stream()
                    .filter(p -> p.getIsActive() == active)
                    .toList();
        }

        return results;
    }

    @Override
    @Transactional(readOnly = true)
    public Product getProductById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy sản phẩm với ID: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public ProductDetailDTO getProductDetail(Long id) {
        Product product = getProductById(id);

        return ProductDetailDTO.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .origin(product.getOrigin())
                .imageUrl(product.getImageUrl())
                .categoryName(product.getCategory() != null
                        ? product.getCategory().getCategoryName() // lazy load ở đây, session còn sống → fine
                        : null)
                .build();
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

        List<ProductVariant> currentVariants = productVariantRepository.findByProduct(product);
        if (request.getVariants() == null || request.getVariants().isEmpty()) {
            productVariantRepository.deleteAll(currentVariants);
        } else {
            Set<Long> requestIds = request.getVariants().stream()
                    .map(ProductVariantRequestDTO::getId)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());

            List<ProductVariant> toDelete = currentVariants.stream()
                    .filter(v -> !requestIds.contains(v.getId()))
                    .collect(Collectors.toList());

            if (!toDelete.isEmpty()) {
                productVariantRepository.deleteAll(toDelete);
            }

            for (ProductVariantRequestDTO variantDTO : request.getVariants()) {
                if (variantDTO.getId() != null) {
                    ProductVariant existing = currentVariants.stream()
                            .filter(v -> v.getId().equals(variantDTO.getId()))
                            .findFirst()
                            .orElse(null);

                    if (existing != null) {
                        existing.setVariantName(variantDTO.getVariantName());
                        existing.setPackaging(variantDTO.getPackaging());
                        existing.setSize(variantDTO.getSize());
                        existing.setPrice(variantDTO.getPrice());
                        existing.setStockQuantity(variantDTO.getStockQuantity());
                        existing.setIsActive(Boolean.TRUE.equals(variantDTO.getIsActive()));
                        productVariantRepository.save(existing);
                    } else {
                        // Trường hợp ID không khớp (có thể do lỗi dữ liệu từ client)
                        ProductVariant variant = buildVariant(variantDTO, product);
                        productVariantRepository.save(variant);
                    }
                } else {
                    ProductVariant variant = buildVariant(variantDTO, product);
                    productVariantRepository.save(variant);
                }
            }
        }

        return product;
    }

    @Override
    @Transactional
    public void deleteProduct(Long id) {
        Product product = getProductById(id);
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

    @Override
    public Long countProducts() {
        return productRepository.count();
    }

    @Override
    public Long countActiveProducts() {
        return productRepository.countByIsActiveTrue();
    }

    @Override
    public Long countLowStockProducts() {
        return productVariantRepository.countByStockQuantityLessThanEqual(10);
    }

    private ProductVariant buildVariant(ProductVariantRequestDTO dto, Product product) {
        return ProductVariant.builder()
                .product(product)
                .variantName(dto.getVariantName())
                .packaging(dto.getPackaging())
                .size(dto.getSize())
                .price(dto.getPrice())
                .stockQuantity(dto.getStockQuantity())
                .isActive(Boolean.TRUE.equals(dto.getIsActive()))
                .build();
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
        List<ProductVariant> variants = productVariantRepository.findByProduct(product);

        List<ProductVariantRequestDTO> variantDTOs = variants.stream()
                .map(v -> ProductVariantRequestDTO.builder()
                        .id(v.getId())
                        .variantName(v.getVariantName())
                        .packaging(v.getPackaging())
                        .size(v.getSize())
                        .price(v.getPrice())
                        .stockQuantity(v.getStockQuantity())
                        .isActive(v.getIsActive())
                        .build())
                .collect(Collectors.toList());

        return ProductRequestDTO.builder()
                .name(product.getName())
                .description(product.getDescription())
                .origin(product.getOrigin())
                .manufacturerDate(product.getManufacturerDate())
                .expiryDate(product.getExpiryDate())
                .categoryId(product.getCategory() != null ? product.getCategory().getId() : null)
                .isActive(product.getIsActive())
                .variants(variantDTOs)
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
                .variants(request.getVariants())
                .build();
    }
}
