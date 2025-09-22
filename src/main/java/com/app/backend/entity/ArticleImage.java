package com.app.backend.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("pd_article_image")
public class ArticleImage {
    private Integer articleId;
    private String imageUrl;
    private Integer orderIndex;
}
