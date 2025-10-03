package com.app.backend.vo;

import lombok.Data;

@Data
public class CommentVO {
    
    /**
     * 评论博文id
     */
    private Long articleId;
    
    /**
     * 评论内容
     */
    private String commentContent;


    private Long userId;
}