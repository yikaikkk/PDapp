package com.app.backend.service.impl;

import com.app.backend.dto.PagedArticleDTO;
import com.app.backend.entity.ArticleImage;
import com.app.backend.mapper.ArticleImageMapper;
import com.app.backend.vo.PagedArticleVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.app.backend.entity.Article;
import com.app.backend.mapper.ArticleMapper;
import com.app.backend.service.ArticleService;
import org.checkerframework.checker.units.qual.A;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ArticleServiceImpl extends ServiceImpl<ArticleMapper, Article> implements ArticleService {
    
    @Autowired
    private ArticleMapper articleMapper;

    @Autowired
    private ArticleImageMapper articleImageMapper;


    @Autowired
    private RabbitTemplate rabbitTemplate;
    
    // 支持的博文类型
    private static final List<String> VALID_TYPES = Arrays.asList(
        "architecture", "nature", "portrait", "street", "night"
    );
    
    @Override
    public Long createArticle(String title, String name, BigDecimal latitude, BigDecimal longitude,
                                String type, String description, String tips, String authorId, String address,String notice,String tools) {
        // 验证必填参数
        if (title == null || title.trim().isEmpty()) {
            return null;
        }
        if (name == null || name.trim().isEmpty()) {
            return null;
        }
        if (latitude == null || longitude == null) {
            return null;
        }
        if (!isValidType(type)) {
            return null;
        }
        if (authorId == null) {
            return null;
        }
        
        // 创建新博文
        Article article = new Article();
        article.setTitle(title.trim());
        article.setName(name.trim());
        article.setLatitude(latitude);
        article.setLongitude(longitude);
        article.setType(type);
        article.setDescription(description != null ? description.trim() : null);
        article.setTips(tips != null ? tips.trim() : null);
        article.setAuthorId(authorId);
        article.setCreateTime(LocalDateTime.now());
        article.setUpdateTime(LocalDateTime.now());
        article.setAddress(address);
        article.setNotice(notice);
        article.setTools(tools);
        
        // 保存到数据库
        int result = articleMapper.insert(article);
        // 返回插入记录的主键ID
        return result > 0 ? article.getId() : null;
    }
    
    @Override
    public List<Article> getArticlesByType(String type) {
        if (!isValidType(type)) {
            return null;
        }
        return articleMapper.findByType(type);
    }
    
    @Override
    public List<Article> getArticlesByAuthor(Integer authorId) {
        if (authorId == null) {
            return null;
        }
        return articleMapper.findByAuthorId(authorId);
    }
    
    @Override
    public IPage<PagedArticleDTO> getArticlesPaged(PagedArticleVO pagedArticleVO) {
//        if (!isValidType(type)) {
//            return null;
//        }
//        if (page == null || page < 1) page = 1;
//        if (size == null || size < 1) size = 10;
        
//        Page<Article> pageObj = new Page<>(page, size);
        LambdaQueryWrapper<Article> articleLambdaQueryWrapper=new LambdaQueryWrapper<>();
        Page<Article> articlePage=new Page<>(pagedArticleVO.getPage(),pagedArticleVO.getSize());

        if(pagedArticleVO.getType() != null && !pagedArticleVO.getType().isEmpty()){
            articleLambdaQueryWrapper.eq(Article::getType,pagedArticleVO.getType());
        }

        if(pagedArticleVO.getTitle() != null && !pagedArticleVO.getTitle().isEmpty()){
            articleLambdaQueryWrapper.like(Article::getTitle, pagedArticleVO.getTitle());
        }

        if(pagedArticleVO.getAddress() != null && !pagedArticleVO.getAddress().isEmpty()){
            articleLambdaQueryWrapper.like(Article::getAddress, pagedArticleVO.getAddress());
        }


        IPage<Article> articles=articleMapper.selectPage(articlePage,articleLambdaQueryWrapper);
        List<PagedArticleDTO> pagedArticleDTOList=new ArrayList<>();
        for(Article article: articles.getRecords()){
            LambdaQueryWrapper<ArticleImage> articleImageLambdaQueryWrapper=new LambdaQueryWrapper<>();
            articleImageLambdaQueryWrapper.eq(ArticleImage::getArticleId,article.getId());
            List<String> articleImages = articleImageMapper.selectList(articleImageLambdaQueryWrapper)
                    .stream()
                    .sorted(Comparator.comparing(ArticleImage::getOrderIndex))
                    .map(ArticleImage::getImageUrl)
                    .collect(Collectors.toList());
            PagedArticleDTO tempPagedArticleDTO=new PagedArticleDTO();
            tempPagedArticleDTO.setAccessUrls(articleImages);
            tempPagedArticleDTO.setType(article.getType());
            tempPagedArticleDTO.setDescription(article.getDescription());
            tempPagedArticleDTO.setAddress(article.getAddress());
            tempPagedArticleDTO.setName(article.getName());
            tempPagedArticleDTO.setTips(article.getTips());
            tempPagedArticleDTO.setAuthorId(article.getAuthorId());
            tempPagedArticleDTO.setLatitude(article.getLatitude());
            tempPagedArticleDTO.setLongitude(article.getLongitude());
            tempPagedArticleDTO.setNotice(article.getNotice());
            tempPagedArticleDTO.setTitle(article.getTitle());
            tempPagedArticleDTO.setTools(article.getTools());
            tempPagedArticleDTO.setId(article.getId());
            pagedArticleDTOList.add(tempPagedArticleDTO);
        }

        IPage<PagedArticleDTO> result=new Page<>(pagedArticleVO.getPage(),pagedArticleVO.getSize());
        result.setRecords(pagedArticleDTOList);

        return result;
    }
    
    @Override
    public IPage<Article> getArticlesByAuthorPaged(Integer page, Integer size, Integer authorId) {
        if (authorId == null) {
            return null;
        }
        if (page == null || page < 1) page = 1;
        if (size == null || size < 1) size = 10;
        
        Page<Article> pageObj = new Page<>(page, size);
        return articleMapper.selectPageByAuthor(pageObj, authorId);
    }
    
    @Override
    public List<Article> getArticlesByLocationRange(Double minLat, Double maxLat, Double minLng, Double maxLng) {
        if (minLat == null || maxLat == null || minLng == null || maxLng == null) {
            return null;
        }
        if (minLat >= maxLat || minLng >= maxLng) {
            return null;
        }
        return articleMapper.findByLocationRange(minLat, maxLat, minLng, maxLng);
    }
    
    @Override
    public boolean updateArticle(Integer id, String title, String name, BigDecimal latitude, BigDecimal longitude, 
                                String type, String description, String tips) {
        if (id == null) {
            return false;
        }
        
        // 获取原博文
        Article article = articleMapper.selectById(id);
        if (article == null) {
            return false;
        }
        
        // 更新字段（只更新非空字段）
        if (title != null && !title.trim().isEmpty()) {
            article.setTitle(title.trim());
        }
        if (name != null && !name.trim().isEmpty()) {
            article.setName(name.trim());
        }
        if (latitude != null) {
            article.setLatitude(latitude);
        }
        if (longitude != null) {
            article.setLongitude(longitude);
        }
        if (isValidType(type)) {
            article.setType(type);
        }
        if (description != null) {
            article.setDescription(description.trim());
        }
        if (tips != null) {
            article.setTips(tips.trim());
        }
        article.setUpdateTime(LocalDateTime.now());
        
        // 保存更新
        int result = articleMapper.updateById(article);
        return result > 0;
    }
    
    @Override
    public boolean deleteArticle(Integer id, Integer authorId) {
        if (id == null || authorId == null) {
            return false;
        }
        
        // 验证博文是否存在且属于该作者
        Article article = articleMapper.selectById(id);
        if (article == null || !article.getAuthorId().equals(authorId)) {
            return false;
        }
        
        // 删除博文
        int result = articleMapper.deleteById(id);
        return result > 0;
    }
    
    @Override
    public boolean isValidType(String type) {
        return type != null && VALID_TYPES.contains(type.toLowerCase());
    }
}