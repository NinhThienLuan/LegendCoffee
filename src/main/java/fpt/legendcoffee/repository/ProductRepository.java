package fpt.legendcoffee.repository;

import fpt.legendcoffee.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {

    Long countByIsActiveTrue();
}
