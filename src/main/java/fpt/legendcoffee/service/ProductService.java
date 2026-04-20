package fpt.legendcoffee.service;

import java.util.List;

import fpt.legendcoffee.dto.request.ProductRequestDTO;
import fpt.legendcoffee.dto.response.ProductResponseDTO;
import fpt.legendcoffee.entity.Category;
import fpt.legendcoffee.entity.Product;

public interface ProductService {
    Product addProduct(ProductRequestDTO request);

    List<Product> getAllProducts();

    List<ProductResponseDTO> getAllProductResponses();

    Product getProductById(Long id);

    Product updateProduct(Long id, ProductRequestDTO request);

    void deleteProduct(Long id);

    ProductRequestDTO getProductRequestById(Long id);

    ProductRequestDTO sanitize(ProductRequestDTO request);

    List<Category> getAllCategories();
}
