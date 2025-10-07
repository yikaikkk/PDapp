package com.app.backend.dto;

import com.app.backend.entity.Article;
import lombok.Data;
import org.apache.commons.logging.Log;

import java.util.List;

@Data
public class PagedArticleDTO extends Article {
    private List<String> accessUrls;

    private Long likeCount;

    private String authorName;

}
