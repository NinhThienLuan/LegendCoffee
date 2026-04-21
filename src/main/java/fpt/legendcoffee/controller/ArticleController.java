package fpt.legendcoffee.controller;

import fpt.legendcoffee.entity.Article;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.time.LocalDateTime;

@Controller
public class ArticleController {

    //test
    @GetMapping("/articles/{id}")
    public String article(@PathVariable Long id, Model model) {
                Article article = new Article();
                article.setTitle("Tối ưu hóa chuỗi cung ứng cà phê trong kỷ nguyên số - Bài #" + id);
                article.setSummary("Bài viết mô phỏng dữ liệu từ model để render nội dung động theo format Editor.js.");
                article.setCreatedAt(LocalDateTime.now());
                article.setCoverImageUrl("https://images.unsplash.com/photo-1495474472287-4d71bcdd2085?auto=format&fit=crop&w=1800&q=80");
                article.setContentJson("""
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

    @GetMapping("/article/detail")
    public String articleDetail(Model model) {
        return "articles/article-detail";
    }
}
