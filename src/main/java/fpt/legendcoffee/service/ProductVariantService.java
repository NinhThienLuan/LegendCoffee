package fpt.legendcoffee.service;

import fpt.legendcoffee.entity.Product;
import fpt.legendcoffee.entity.ProductVariant;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface ProductVariantService {
    List<ProductVariant> getVariantsByProductId(Long productId);

    List<ProductVariant> findByProduct(Product product);
}
