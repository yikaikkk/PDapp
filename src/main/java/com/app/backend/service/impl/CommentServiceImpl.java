package com.app.backend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.app.backend.entity.Comment;
import com.app.backend.mapper.CommentMapper;
import com.app.backend.service.CommentService;
import org.springframework.stereotype.Service;

/**
 * 评论服务实现类
 */
@Service
public class CommentServiceImpl extends ServiceImpl<CommentMapper, Comment> implements CommentService {
    // 可以添加自定义的业务实现
}