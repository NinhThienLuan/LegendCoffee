package fpt.legendcoffee.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import fpt.legendcoffee.entity.Article;

@Repository
public interface ArticleRepository extends JpaRepository<Article, Long> {

    Optional<Article> findById(Long id);

    List<Article> findAll();

    void deleteById(Long id);
}
