package com.app.backend.dto;

import com.app.backend.entity.Article;
import lombok.Data;

import java.util.List;

@Data
public class PagedArticleDTO extends Article {
    private List<String> accessUrls;

}
