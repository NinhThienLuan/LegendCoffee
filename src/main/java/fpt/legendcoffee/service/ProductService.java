package fpt.legendcoffee.service;

import fpt.legendcoffee.dto.request.ProductRequestDTO;
import fpt.legendcoffee.entity.Product;

public interface ProductService {
	Product addProduct(ProductRequestDTO request);
}
