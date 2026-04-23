package fpt.legendcoffee.service.serviceImpl;

import java.util.List;

import org.springframework.stereotype.Service;

import fpt.legendcoffee.entity.Article;
import fpt.legendcoffee.repository.ArticleRepository;
import fpt.legendcoffee.service.ArticleService;
import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class ArticleServiceImpl implements ArticleService {

    private final ArticleRepository articleRepository;

    @Override
    public List<Article> getAllArticles() {
        return articleRepository.findAll();
    }

    @Override
    public List<Article> getPublishedArticles() {
        return articleRepository.findByStatusOrderByPublishedAtDesc(fpt.legendcoffee.entity.enumeration.ArticleStatus.PUBLISHED);
    }

    @Override
    public Article getArticleById(Long id) {
        return articleRepository.findById(id).orElse(null);
    }

    @Override
    public Article createArticle(Article article) {
        return articleRepository.save(article);
    }

    @Override
    public Article updateArticle(Article article) {
        return articleRepository.save(article);
    }

    @Override
    public void deleteArticle(Long id) {
        articleRepository.deleteById(id);
    }
}
