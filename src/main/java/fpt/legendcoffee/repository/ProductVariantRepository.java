package fpt.legendcoffee.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.stereotype.Repository;

import fpt.legendcoffee.entity.Product;
import fpt.legendcoffee.entity.ProductVariant;

@Repository
public interface ProductVariantRepository extends JpaRepository<ProductVariant, Long> {

    /** Lấy danh sách biến thể theo sản phẩm (dùng để hiển thị khi edit). */
    List<ProductVariant> findByProduct(Product product);

    /** Xóa tất cả biến thể của một sản phẩm (dùng khi update). */
    void deleteByProduct(Product product);

    @SuppressWarnings("unused")
    @EntityGraph(attributePaths = { "product" })
    List<ProductVariant> findAllBy();
}
