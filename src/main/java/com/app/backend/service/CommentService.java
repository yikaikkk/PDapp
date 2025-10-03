package com.app.backend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.app.backend.entity.Comment;

import java.util.List;

/**
 * 评论服务接口
 */
public interface CommentService extends IService<Comment> {
    // 可以添加自定义的业务方法

    List<Comment> getCommentsByArticleId(Long articleId);
}