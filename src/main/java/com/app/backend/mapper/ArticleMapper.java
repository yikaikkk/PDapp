package com.app.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.app.backend.entity.Article;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ArticleMapper extends BaseMapper<Article> {
    
    /**
     * 根据类型查询博文列表
     * @param type 博文类型
     * @return 博文列表
     */
    List<Article> findByType(@Param("type") String type);
    
    /**
     * 根据作者ID查询博文列表
     * @param authorId 作者ID
     * @return 博文列表
     */
    List<Article> findByAuthorId(@Param("authorId") Integer authorId);
    
    /**
     * 分页查询博文（按类型）
     * @param page 分页对象
     * @param type 博文类型
     * @return 分页结果
     */
    IPage<Article> selectPageByType(Page<Article> page, @Param("type") String type);
    
    /**
     * 分页查询博文（按作者）
     * @param page 分页对象
     * @param authorId 作者ID
     * @return 分页结果
     */
    IPage<Article> selectPageByAuthor(Page<Article> page, @Param("authorId") Integer authorId);
    
    /**
     * 根据地理位置范围查询博文
     * @param minLat 最小纬度
     * @param maxLat 最大纬度
     * @param minLng 最小经度
     * @param maxLng 最大经度
     * @return 博文列表
     */
    List<Article> findByLocationRange(@Param("minLat") Double minLat, 
                                     @Param("maxLat") Double maxLat,
                                     @Param("minLng") Double minLng, 
                                     @Param("maxLng") Double maxLng);
}