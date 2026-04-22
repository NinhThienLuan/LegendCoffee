package fpt.legendcoffee.controller;

import fpt.legendcoffee.common.mapper.ArticleMapper;
import fpt.legendcoffee.dto.request.ArticleRequestDTO;
import fpt.legendcoffee.dto.response.ArticleResponseDTO;
import fpt.legendcoffee.entity.Article;
import fpt.legendcoffee.entity.enumeration.ArticleStatus;
import fpt.legendcoffee.repository.UserRepository;
import fpt.legendcoffee.service.ArticleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import jakarta.validation.Valid;
import org.springframework.validation.BindingResult;
import fpt.legendcoffee.dto.request.ArticleRequestDTO;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class ArticleController {

    private final ArticleService articleService;
    private final UserRepository userRepository;

    @GetMapping("/articles/{id}")
    public String article(@PathVariable Long id, Model model) {
        Article article = new Article();
        article.setTitle("Tối ưu hóa chuỗi cung ứng cà phê trong kỷ nguyên số - Bài #" + id);
        article.setSummary("Bài viết mô phỏng dữ liệu từ model để render nội dung động theo format Editor.js.");
        article.setCreatedAt(LocalDateTime.now());
        article.setCoverImageUrl(
                "https://images.unsplash.com/photo-1495474472287-4d71bcdd2085?auto=format&fit=crop&w=1800&q=80");
        article.setContentJson(
                """
                        {
                            "time": 1713550000000,
                            "version": "2.29.1",
                            "blocks": [
                                {
                                    "type": "header",
                                    "data": {
                                        "text": "Tư duy vận hành hiện đại cho ngành cà phê",
                                        "level": 2
                                    }
                                },
                                {
                                    "type": "paragraph",
                                    "data": {
                                        "text": "Doanh nghiệp B2B cần kết nối rang xay, kho vận và dữ liệu thời gian thực để giảm rủi ro và tăng hiệu suất."
                                    }
                                },
                                {
                                    "type": "image",
                                    "data": {
                                        "url": "https://images.unsplash.com/photo-1511920170033-f8396924c348?auto=format&fit=crop&w=1200&q=80",
                                        "caption": "Theo dõi chất lượng hạt và dữ liệu vận hành theo thời gian thực"
                                    }
                                },
                                {
                                    "type": "list",
                                    "data": {
                                        "style": "unordered",
                                        "items": [
                                            "Theo dõi tồn kho theo lô hàng",
                                            "Chuẩn hóa chất lượng theo profile rang",
                                            "Tối ưu chi phí logistics liên vùng"
                                        ]
                                    }
                                },
                                {
                                    "type": "quote",
                                    "data": {
                                        "text": "Dữ liệu tốt giúp quyết định nhanh và đúng trong chuỗi cung ứng.",
                                        "caption": "RoastLogistics Insight"
                                    }
                                }
                            ]
                        }
                        """);

        model.addAttribute("article", article);
        return "articles/articles";
    }

    @GetMapping("/articles/detail")
    public String articleDetail(Model model) {
        return "articles/articles";
    }

    @GetMapping("/admin/articles/view/{id}")
    public String viewArticle(@PathVariable Long id, Model model) {
        Article article = articleService.getArticleById(id);
        if (article == null) {
            return "redirect:/admin/posts";
        }
        model.addAttribute("article", article);
        return "articles/articles"; // This points to your existing template
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
                userRepository.findByEmail(principal.getName()).ifPresent(article::setUser);
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
