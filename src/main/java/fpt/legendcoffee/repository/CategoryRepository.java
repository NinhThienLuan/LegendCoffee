package fpt.legendcoffee.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import fpt.legendcoffee.entity.Category;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
}
