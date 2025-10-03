package com.app.backend.vo;

import com.app.backend.entity.Article;
import lombok.Data;

@Data
public class ArticleDetailVO extends Article {
    private Long likeCount;
}
