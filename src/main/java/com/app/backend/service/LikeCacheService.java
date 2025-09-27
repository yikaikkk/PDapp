package com.app.backend.service;

import java.util.Map;

/**
 * 点赞缓存服务接口
 */
public interface LikeCacheService {
    
    /**
     * 缓存所有用户的点赞数量
     * @param userLikeCounts 用户ID -> 点赞数量的映射
     */
    void cacheUserLikeCounts(Map<Long, Integer> userLikeCounts);
    
    /**
     * 缓存所有博文的点赞数量
     * @param articleLikeCounts 博文ID -> 点赞数量的映射
     */
    void cacheArticleLikeCounts(Map<Integer, Integer> articleLikeCounts);
    
    /**
     * 获取用户的点赞数量（从缓存）
     * @param userId 用户ID
     * @return 点赞数量
     */
    Integer getUserLikeCount(Long userId);
    
    /**
     * 获取博文的点赞数量（从缓存）
     * @param articleId 博文ID
     * @return 点赞数量
     */
    Integer getArticleLikeCount(Integer articleId);
    
    /**
     * 更新单个用户的点赞数量缓存
     * @param userId 用户ID
     * @param likeCount 点赞数量
     */
    void updateUserLikeCount(Long userId, Integer likeCount);
    
    /**
     * 更新单个博文的点赞数量缓存
     * @param articleId 博文ID
     * @param likeCount 点赞数量
     */
    void updateArticleLikeCount(Integer articleId, Integer likeCount);
    
    /**
     * 删除用户点赞数量缓存
     * @param userId 用户ID
     */
    void deleteUserLikeCount(Long userId);
    
    /**
     * 删除博文点赞数量缓存
     * @param articleId 博文ID
     */
    void deleteArticleLikeCount(Integer articleId);
    
    /**
     * 清空所有点赞缓存
     */
    void clearAllLikeCaches();
    
    /**
     * 获取缓存统计信息
     * @return 统计信息
     */
    Map<String, Object> getCacheStatistics();
}