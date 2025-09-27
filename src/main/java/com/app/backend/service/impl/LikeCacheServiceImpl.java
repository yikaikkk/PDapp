package com.app.backend.service.impl;

import com.app.backend.service.LikeCacheService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * 点赞缓存服务实现类
 */
@Slf4j
@Service
public class LikeCacheServiceImpl implements LikeCacheService {
    
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    
    // Redis key前缀常量
    private static final String USER_LIKE_COUNT_PREFIX = "like:user:count:";
    private static final String ARTICLE_LIKE_COUNT_PREFIX = "like:article:count:";
    private static final String CACHE_EXPIRE_TIME = "like:cache:expire_time";
    
    // 缓存过期时间（24小时）
    private static final long CACHE_EXPIRE_HOURS = 24;
    
    @Override
    public void cacheUserLikeCounts(Map<Long, Integer> userLikeCounts) {
        if (userLikeCounts == null || userLikeCounts.isEmpty()) {
            log.warn("用户点赞数据为空，跳过缓存");
            return;
        }
        
        try {
            // 批量设置用户点赞数量
            for (Map.Entry<Long, Integer> entry : userLikeCounts.entrySet()) {
                String key = USER_LIKE_COUNT_PREFIX + entry.getKey();
                redisTemplate.opsForValue().set(key, entry.getValue(), CACHE_EXPIRE_HOURS, TimeUnit.HOURS);
            }
            
            // 记录缓存更新时间
            redisTemplate.opsForValue().set(CACHE_EXPIRE_TIME + ":user", System.currentTimeMillis(), 
                                          CACHE_EXPIRE_HOURS, TimeUnit.HOURS);
            
            log.info("成功缓存 {} 个用户的点赞数据", userLikeCounts.size());
        } catch (Exception e) {
            log.error("缓存用户点赞数据失败", e);
        }
    }
    
    @Override
    public void cacheArticleLikeCounts(Map<Integer, Integer> articleLikeCounts) {
        if (articleLikeCounts == null || articleLikeCounts.isEmpty()) {
            log.warn("博文点赞数据为空，跳过缓存");
            return;
        }
        
        try {
            // 批量设置博文点赞数量
            for (Map.Entry<Integer, Integer> entry : articleLikeCounts.entrySet()) {
                String key = ARTICLE_LIKE_COUNT_PREFIX + entry.getKey();
                redisTemplate.opsForValue().set(key, entry.getValue(), CACHE_EXPIRE_HOURS, TimeUnit.HOURS);
            }
            
            // 记录缓存更新时间
            redisTemplate.opsForValue().set(CACHE_EXPIRE_TIME + ":article", System.currentTimeMillis(), 
                                          CACHE_EXPIRE_HOURS, TimeUnit.HOURS);
            
            log.info("成功缓存 {} 个博文的点赞数据", articleLikeCounts.size());
        } catch (Exception e) {
            log.error("缓存博文点赞数据失败", e);
        }
    }
    
    @Override
    public Integer getUserLikeCount(Long userId) {
        if (userId == null) {
            return 0;
        }
        
        try {
            String key = USER_LIKE_COUNT_PREFIX + userId;
            Object value = redisTemplate.opsForValue().get(key);
            return value != null ? Integer.valueOf(value.toString()) : 0;
        } catch (Exception e) {
            log.error("获取用户点赞数量缓存失败, userId: {}", userId, e);
            return 0;
        }
    }
    
    @Override
    public Integer getArticleLikeCount(Integer articleId) {
        if (articleId == null) {
            return 0;
        }
        
        try {
            String key = ARTICLE_LIKE_COUNT_PREFIX + articleId;
            Object value = redisTemplate.opsForValue().get(key);
            return value != null ? Integer.valueOf(value.toString()) : 0;
        } catch (Exception e) {
            log.error("获取博文点赞数量缓存失败, articleId: {}", articleId, e);
            return 0;
        }
    }
    
    @Override
    public void updateUserLikeCount(Long userId, Integer likeCount) {
        if (userId == null || likeCount == null) {
            return;
        }
        
        try {
            String key = USER_LIKE_COUNT_PREFIX + userId;
            redisTemplate.opsForValue().set(key, likeCount, CACHE_EXPIRE_HOURS, TimeUnit.HOURS);
        } catch (Exception e) {
            log.error("更新用户点赞数量缓存失败, userId: {}, likeCount: {}", userId, likeCount, e);
        }
    }
    
    @Override
    public void updateArticleLikeCount(Integer articleId, Integer likeCount) {
        if (articleId == null || likeCount == null) {
            return;
        }
        
        try {
            String key = ARTICLE_LIKE_COUNT_PREFIX + articleId;
            redisTemplate.opsForValue().set(key, likeCount, CACHE_EXPIRE_HOURS, TimeUnit.HOURS);
        } catch (Exception e) {
            log.error("更新博文点赞数量缓存失败, articleId: {}, likeCount: {}", articleId, likeCount, e);
        }
    }
    
    @Override
    public void deleteUserLikeCount(Long userId) {
        if (userId == null) {
            return;
        }
        
        try {
            String key = USER_LIKE_COUNT_PREFIX + userId;
            redisTemplate.delete(key);
        } catch (Exception e) {
            log.error("删除用户点赞数量缓存失败, userId: {}", userId, e);
        }
    }
    
    @Override
    public void deleteArticleLikeCount(Integer articleId) {
        if (articleId == null) {
            return;
        }
        
        try {
            String key = ARTICLE_LIKE_COUNT_PREFIX + articleId;
            redisTemplate.delete(key);
        } catch (Exception e) {
            log.error("删除博文点赞数量缓存失败, articleId: {}", articleId, e);
        }
    }
    
    @Override
    public void clearAllLikeCaches() {
        try {
            // 删除所有用户点赞缓存
            Set<String> userKeys = redisTemplate.keys(USER_LIKE_COUNT_PREFIX + "*");
            if (userKeys != null && !userKeys.isEmpty()) {
                redisTemplate.delete(userKeys);
            }
            
            // 删除所有博文点赞缓存
            Set<String> articleKeys = redisTemplate.keys(ARTICLE_LIKE_COUNT_PREFIX + "*");
            if (articleKeys != null && !articleKeys.isEmpty()) {
                redisTemplate.delete(articleKeys);
            }
            
            // 删除缓存时间记录
            redisTemplate.delete(CACHE_EXPIRE_TIME + ":user");
            redisTemplate.delete(CACHE_EXPIRE_TIME + ":article");
            
            log.info("成功清空所有点赞缓存");
        } catch (Exception e) {
            log.error("清空点赞缓存失败", e);
        }
    }
    
    @Override
    public Map<String, Object> getCacheStatistics() {
        Map<String, Object> statistics = new HashMap<>();
        
        try {
            // 统计用户点赞缓存数量
            Set<String> userKeys = redisTemplate.keys(USER_LIKE_COUNT_PREFIX + "*");
            int userCacheCount = userKeys != null ? userKeys.size() : 0;
            
            // 统计博文点赞缓存数量
            Set<String> articleKeys = redisTemplate.keys(ARTICLE_LIKE_COUNT_PREFIX + "*");
            int articleCacheCount = articleKeys != null ? articleKeys.size() : 0;
            
            // 获取缓存更新时间
            Object userCacheTime = redisTemplate.opsForValue().get(CACHE_EXPIRE_TIME + ":user");
            Object articleCacheTime = redisTemplate.opsForValue().get(CACHE_EXPIRE_TIME + ":article");
            
            statistics.put("userCacheCount", userCacheCount);
            statistics.put("articleCacheCount", articleCacheCount);
            statistics.put("totalCacheCount", userCacheCount + articleCacheCount);
            statistics.put("userCacheUpdateTime", userCacheTime != null ? Long.valueOf(userCacheTime.toString()) : null);
            statistics.put("articleCacheUpdateTime", articleCacheTime != null ? Long.valueOf(articleCacheTime.toString()) : null);
            
        } catch (Exception e) {
            log.error("获取缓存统计信息失败", e);
            statistics.put("error", e.getMessage());
        }
        
        return statistics;
    }
}