package com.app.backend.service.impl;

import com.app.backend.entity.ArticleImage;
import com.app.backend.mapper.ArticleImageMapper;
import com.app.backend.service.ArticleImageService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class ArticleServiceImageImpl extends ServiceImpl<ArticleImageMapper, ArticleImage> implements ArticleImageService {
}
