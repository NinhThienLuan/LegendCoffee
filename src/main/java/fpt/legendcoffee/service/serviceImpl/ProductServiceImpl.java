package fpt.legendcoffee.service.serviceImpl;

import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fpt.legendcoffee.dto.request.ProductRequestDTO;
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
        String imageUrl = null;
        String imagePublicId = null;

        if (request.getImage() != null && !request.getImage().isEmpty()) {
            Map<String, Object> uploadResult = imageService.upload(request.getImage());
            Object secureUrl = uploadResult.get("secure_url");
            Object publicId = uploadResult.get("public_id");
            imageUrl = secureUrl == null ? null : secureUrl.toString();
            imagePublicId = publicId == null ? null : publicId.toString();
        }

        Product product = Product.builder()
                .name(request.getName() == null ? null : request.getName().trim())
                .description(request.getDescription())
                .origin(request.getOrigin())
                .expiryDate(request.getExpiryDate())
                .manufacturerDate(request.getManufacturerDate())
                .imageUrl(imageUrl)
                .imagePublicId(imagePublicId)
                .isActive(request.getIsActive() == null ? Boolean.TRUE : request.getIsActive())
                .build();

        return productRepository.save(product);
    }
}