package com.app.backend.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 评论实体类
 */
@Data
@TableName("pd_comment")
public class Comment implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(type = IdType.AUTO)
    private Integer id;
    
    /**
     * 评论用户Id
     */
    private Long userId;
    
    /**
     * 评论博文id
     */
    private Long articleId;
    
    /**
     * 评论内容
     */
    private String commentContent;
    
    /**
     * 是否删除 0否 1是
     */
    private Integer isDelete;

    
    /**
     * 评论时间
     */
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}