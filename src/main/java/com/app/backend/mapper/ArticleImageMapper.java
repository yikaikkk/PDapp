package com.app.backend.mapper;

import com.app.backend.entity.Article;
import com.app.backend.entity.ArticleImage;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.jmx.export.annotation.ManagedOperation;

@Mapper
public interface ArticleImageMapper extends BaseMapper<ArticleImage> {
}
