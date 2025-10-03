package com.app.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.app.backend.entity.Comment;
import org.apache.ibatis.annotations.Mapper;

/**
 * 评论Mapper接口
 */
@Mapper
public interface CommentMapper extends BaseMapper<Comment> {
    // 可以添加自定义的SQL查询方法
}