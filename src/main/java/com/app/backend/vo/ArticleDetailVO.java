package com.app.backend.vo;

import com.app.backend.entity.Article;
import com.app.backend.entity.Comment;
import lombok.Data;

import java.util.List;

@Data
public class ArticleDetailVO extends Article {
    private Long likeCount;

    private List<Comment> comments;
}
