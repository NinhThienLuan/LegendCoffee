package fpt.legendcoffee.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ArticleController {
    @GetMapping("/article")
    public String article(Model model) {
        return "articles/articles";
    }

    @GetMapping("/article/detail")
    public String articleDetail(Model model) {
        return "articles/article-detail";
    }
}
