package fpt.legendcoffee.service;

import java.util.List;

import fpt.legendcoffee.dto.request.ProductRequestDTO;
import fpt.legendcoffee.entity.Product;

public interface ProductService {
	Product addProduct(ProductRequestDTO request);

	List<Product> getAllProducts();

	Product getProductById(Long id);

	Product updateProduct(Long id, ProductRequestDTO request);

	void deleteProduct(Long id);
}
