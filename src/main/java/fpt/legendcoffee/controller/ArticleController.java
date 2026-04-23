package fpt.legendcoffee.controller;

import fpt.legendcoffee.common.mapper.ArticleMapper;
import fpt.legendcoffee.dto.request.ArticleRequestDTO;
import fpt.legendcoffee.dto.response.ArticleResponseDTO;
import fpt.legendcoffee.entity.Article;
import fpt.legendcoffee.entity.enumeration.ArticleStatus;
import fpt.legendcoffee.repository.UserRepository;
import fpt.legendcoffee.service.ArticleService;
import fpt.legendcoffee.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class ArticleController {

    private final ArticleService articleService;
    private final UserService userService;

    @GetMapping("/articles/{id}")
    public String article(@PathVariable Long id, Model model) {
        Article article = articleService.getArticleById(id);
        if (article == null) {
            return "redirect:/articles";
        }
        model.addAttribute("article", article);

        // Fetch related articles (top 3 published, excluding current)
        List<Article> relatedArticles = articleService.getPublishedArticles()
                .stream()
                .filter(a -> !a.getId().equals(id))
                .limit(3)
                .toList();
        model.addAttribute("relatedArticles", relatedArticles);

        return "articles/detail";
    }

    @GetMapping("/articles")
    public String articleList(Model model) {
        List<ArticleResponseDTO> articles = articleService.getAllArticles()
                .stream()
                .map(ArticleMapper::toResponseDTO)
                .toList();
        model.addAttribute("articles", articles);
        return "articles/list";
    }

    @GetMapping("/admin/articles/view/{id}")
    public String viewArticle(@PathVariable Long id, Model model) {
        Article article = articleService.getArticleById(id);
        if (article == null) {
            return "redirect:/admin/posts";
        }
        model.addAttribute("article", article);
        return "articles/detail"; // This points to your existing template
    }

    @GetMapping("/admin/posts")
    public String adminPosts(Model model) {
        List<ArticleResponseDTO> articles = articleService.getAllArticles()
                .stream()
                .map(ArticleMapper::toResponseDTO)
                .toList();
        model.addAttribute("articles", articles);
        return "admin/posts";
    }

    @GetMapping("/admin/articles/new")
    public String adminPostForm(Model model) {
        model.addAttribute("article", new ArticleRequestDTO());
        return "admin/article-form";
    }

    @GetMapping("/admin/articles/edit/{id}")
    public String editArticle(@PathVariable Long id, Model model) {
        Article article = articleService.getArticleById(id);
        if (article == null) {
            return "redirect:/admin/posts";
        }
        // Sử dụng Mapper để chuyển sang DTO
        ArticleRequestDTO dto = ArticleMapper.toRequestDTO(article);

        model.addAttribute("article", dto);
        return "admin/article-form";
    }

    @PostMapping("/admin/articles/save")
    public String saveArticle(@Valid @ModelAttribute("article") ArticleRequestDTO req, BindingResult bindingResult,
            Principal principal, Model model) {
        if (bindingResult.hasErrors()) {
            return "admin/article-form";
        }

        try {
            Article article;
            if (req.getId() != null) {
                // Trường hợp Cập nhật: Lấy entity cũ từ DB lên
                article = articleService.getArticleById(req.getId());
                if (article == null)
                    article = new Article();
            } else {
                // Trường hợp Tạo mới
                article = new Article();
            }
            // 2. Dùng Mapper để gán dữ liệu từ DTO sang Entity
            ArticleMapper.updateEntity(article, req);
            if (principal != null) {
                userService.findByEmail(principal.getName()).ifPresent(article::setUser);
            }

            if (article.getStatus() == ArticleStatus.PUBLISHED && article.getPublishedAt() == null) {
                article.setPublishedAt(LocalDateTime.now());
            }

            if (article.getId() == null) {
                articleService.createArticle(article);
            } else {
                articleService.updateArticle(article);
            }
            return "redirect:/admin/posts";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "admin/article-form";
        }
    }

    @PostMapping("/admin/articles/delete/{id}")
    public String deleteArticle(@PathVariable Long id) {
        articleService.deleteArticle(id);
        return "redirect:/admin/posts";
    }
}
