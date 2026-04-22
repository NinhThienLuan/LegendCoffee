package fpt.legendcoffee.service;

import java.util.List;

import fpt.legendcoffee.entity.Article;

public interface ArticleService {

    public List<Article> getAllArticles();

    public List<Article> getPublishedArticles();

    public Article getArticleById(Long id);

    public Article createArticle(Article article);

    public Article updateArticle(Article article);

    public void deleteArticle(Long id);
}
