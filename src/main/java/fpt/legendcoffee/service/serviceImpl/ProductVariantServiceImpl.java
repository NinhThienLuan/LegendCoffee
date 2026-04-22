package fpt.legendcoffee.service.serviceImpl;

import fpt.legendcoffee.entity.Product;
import fpt.legendcoffee.entity.ProductVariant;
import fpt.legendcoffee.repository.ProductRepository;
import fpt.legendcoffee.repository.ProductVariantRepository;
import fpt.legendcoffee.service.ProductVariantService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductVariantServiceImpl implements ProductVariantService {
    private final ProductVariantRepository productVariantRepository;
    private final ProductRepository productRepository;

    @Override
    @Transactional(readOnly = true)
    public List<ProductVariant> getVariantsByProductId(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy sản phẩm với ID: " + productId));
        return productVariantRepository.findByProduct(product);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductVariant> findByProduct(Product product) {
        return productVariantRepository.findByProduct(product);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductVariant> getAllVariants() {
        return productVariantRepository.findAllBy();
    }
}
