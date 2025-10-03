package com.app.backend.dto;

import lombok.Data;

@Data
public class ArticleLikeDTO {
    private Integer article;
    private Integer likeCount;
}
