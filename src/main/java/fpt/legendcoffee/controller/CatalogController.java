package fpt.legendcoffee.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class CatalogController {

    // for view access only
    @GetMapping("/")
    public String getHome() {
        return "index";
    }

    // for view access only
    @GetMapping("/catalogs")
    public String getCatalogs() {
        return "catalogs";
    }

    // for view access only
    @GetMapping("/catalogs/{id}")
    public String getCatalogDetails(@PathVariable long id) {
        return "catalog-detail";
    }

    // for view access only
    @GetMapping("/articles")
    public String getArticles() {
        return "editor-lab";
    }

}
