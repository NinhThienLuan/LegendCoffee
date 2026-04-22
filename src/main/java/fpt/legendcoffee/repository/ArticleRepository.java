package fpt.legendcoffee.repository;

import java.util.List;
import java.util.Optional;

import fpt.legendcoffee.entity.Article;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ArticleRepository extends JpaRepository<Article, Long> {

    @Query("SELECT a FROM Article a LEFT JOIN FETCH a.user WHERE a.id = :id")
    Optional<Article> findById(Long id);

    @Query("SELECT a FROM Article a LEFT JOIN FETCH a.user")
    List<Article> findAll();

    @Query("SELECT a FROM Article a LEFT JOIN FETCH a.user WHERE a.status = :status ORDER BY a.publishedAt DESC")
    List<Article> findByStatusOrderByPublishedAtDesc(fpt.legendcoffee.entity.enumeration.ArticleStatus status);

    void deleteById(Long id);
}
