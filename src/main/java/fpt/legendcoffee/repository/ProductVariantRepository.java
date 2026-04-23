package fpt.legendcoffee.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import fpt.legendcoffee.entity.Product;
import fpt.legendcoffee.entity.ProductVariant;

@Repository
public interface ProductVariantRepository extends JpaRepository<ProductVariant, Long> {

    /** Lấy danh sách biến thể theo sản phẩm (dùng để hiển thị khi edit). */
    List<ProductVariant> findByProduct(Product product);
    List<ProductVariant> findByProductAndIsActiveTrue(Product product);

    /** Xóa tất cả biến thể của một sản phẩm (dùng khi update). */
    void deleteByProduct(Product product);

    @SuppressWarnings("unused")
    @EntityGraph(attributePaths = { "product" })
    List<ProductVariant> findAllBy();

    Long countByStockQuantityLessThanEqual(int i);

    /** Tìm kiếm biến thể theo tên sản phẩm và trạng thái active. */
    @Query("""
        SELECT v FROM ProductVariant v
        JOIN FETCH v.product p
        WHERE (:keyword IS NULL OR :keyword = '' OR LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
               OR LOWER(v.variantName) LIKE LOWER(CONCAT('%', :keyword, '%')))
          AND (:active IS NULL OR v.isActive = :active)
        ORDER BY p.name ASC, v.variantName ASC
    """)
    List<ProductVariant> searchVariants(@Param("keyword") String keyword, @Param("active") Boolean active);
}

