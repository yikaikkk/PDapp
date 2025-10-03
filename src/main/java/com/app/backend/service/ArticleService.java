package com.app.backend.service;

import com.app.backend.dto.PagedArticleDTO;
import com.app.backend.vo.PagedArticleVO;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.app.backend.entity.Article;

import java.math.BigDecimal;
import java.util.List;

public interface ArticleService extends IService<Article> {
    
    /**
     * 创建博文
     * @param title 标题
     * @param name 地点名称
     * @param latitude 纬度
     * @param longitude 经度
     * @param type 类型
     * @param description 描述
     * @param tips 摄影提示
     * @param authorId 作者ID
     * @return 插入记录的主键ID，创建失败返回null
     */
    Long createArticle(String title, String name, BigDecimal latitude, BigDecimal longitude,
                         String type, String description, String tips, String authorId, String address,String notice,String tools);
    
    /**
     * 根据类型查询博文列表
     * @param type 博文类型
     * @return 博文列表
     */
    List<Article> getArticlesByType(String type);
    
    /**
     * 根据作者ID查询博文列表
     * @param authorId 作者ID
     * @return 博文列表
     */
    List<Article> getArticlesByAuthor(Integer authorId);
    
    /**
     * 分页查询博文（按类型）
     * @return 分页结果
     */
    IPage<PagedArticleDTO> getArticlesPaged(PagedArticleVO pagedArticleVO);
    
    /**
     * 分页查询博文（按作者）
     * @param page 页码
     * @param size 每页大小
     * @param authorId 作者ID
     * @return 分页结果
     */
    IPage<Article> getArticlesByAuthorPaged(Integer page, Integer size, Integer authorId);
    
    /**
     * 根据地理位置范围查询博文
     * @param minLat 最小纬度
     * @param maxLat 最大纬度
     * @param minLng 最小经度
     * @param maxLng 最大经度
     * @return 博文列表
     */
    List<Article> getArticlesByLocationRange(Double minLat, Double maxLat, Double minLng, Double maxLng);
    
    /**
     * 更新博文信息
     * @param id 博文ID
     * @param title 标题
     * @param name 地点名称
     * @param latitude 纬度
     * @param longitude 经度
     * @param type 类型
     * @param description 描述
     * @param tips 摄影提示
     * @return 是否更新成功
     */
    boolean updateArticle(Integer id, String title, String name, BigDecimal latitude, BigDecimal longitude, 
                         String type, String description, String tips);
    
    /**
     * 根据ID删除博文
     * @param id 博文ID
     * @param authorId 作者ID（用于权限验证）
     * @return 是否删除成功
     */
    boolean deleteArticle(Integer id, Integer authorId);
    
    /**
     * 验证博文类型是否有效
     * @param type 类型
     * @return 是否有效
     */
    boolean isValidType(String type);
}