package com.app.backend.task;

import com.app.backend.mapper.LikeMapper;
import com.app.backend.service.LikeCacheService;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 点赞统计定时任务
 * 从MySQL的like表中汇集点赞信息并存储到Redis中
 */
@Slf4j
@Component
public class LikeStatisticsJob implements Job {
    
    @Autowired
    private LikeMapper likeMapper;
    
    @Autowired
    private LikeCacheService likeCacheService;
    
    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        log.info("开始执行点赞统计任务");
        
        long startTime = System.currentTimeMillis();
        
        try {
            // 1. 统计每个用户的点赞数量
            Map<Long, Integer> userLikeCounts = getUserLikeCounts();
            
            // 2. 统计每个博文的点赞数量
            Map<Integer, Integer> articleLikeCounts = getArticleLikeCounts();
            
            // 3. 将统计数据缓存到Redis
            if (!userLikeCounts.isEmpty()) {
                likeCacheService.cacheUserLikeCounts(userLikeCounts);
            }
            
            if (!articleLikeCounts.isEmpty()) {
                likeCacheService.cacheArticleLikeCounts(articleLikeCounts);
            }
            
            long endTime = System.currentTimeMillis();
            log.info("点赞统计任务执行完成，耗时: {} ms，用户数: {}，博文数: {}", 
                    endTime - startTime, userLikeCounts.size(), articleLikeCounts.size());
            
        } catch (Exception e) {
            log.error("点赞统计任务执行失败", e);
            throw new JobExecutionException("点赞统计任务执行失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 统计每个用户的点赞数量
     * @return 用户ID -> 点赞数量的映射
     */
    private Map<Long, Integer> getUserLikeCounts() {
        Map<Long, Integer> userLikeCounts = new HashMap<>();
        
        try {
            // 查询所有有效点赞的用户统计
            String sql = "SELECT user_id, COUNT(*) as like_count FROM pd_like WHERE status = 1 GROUP BY user_id";
            List<Map<String, Object>> results = likeMapper.selectMaps(
                    new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<com.app.backend.entity.Like>()
                            .select("user_id", "COUNT(*) as like_count")
                            .eq("status", 1)
                            .groupBy("user_id")
            );
            
            for (Map<String, Object> result : results) {
                Long userId = Long.valueOf(result.get("user_id").toString());
                Integer likeCount = Integer.valueOf(result.get("like_count").toString());
                userLikeCounts.put(userId, likeCount);
            }
            
            log.debug("统计到 {} 个用户的点赞数据", userLikeCounts.size());
            
        } catch (Exception e) {
            log.error("统计用户点赞数量失败", e);
        }
        
        return userLikeCounts;
    }
    
    /**
     * 统计每个博文的点赞数量
     * @return 博文ID -> 点赞数量的映射
     */
    private Map<Integer, Integer> getArticleLikeCounts() {
        Map<Integer, Integer> articleLikeCounts = new HashMap<>();
        
        try {
            // 查询所有博文的点赞统计
            List<Map<String, Object>> results = likeMapper.selectMaps(
                    new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<com.app.backend.entity.Like>()
                            .select("article_id", "COUNT(*) as like_count")
                            .eq("status", 1)
                            .groupBy("article_id")
            );
            
            for (Map<String, Object> result : results) {
                Integer articleId = Integer.valueOf(result.get("article_id").toString());
                Integer likeCount = Integer.valueOf(result.get("like_count").toString());
                articleLikeCounts.put(articleId, likeCount);
            }
            
            log.debug("统计到 {} 个博文的点赞数据", articleLikeCounts.size());
            
        } catch (Exception e) {
            log.error("统计博文点赞数量失败", e);
        }
        
        return articleLikeCounts;
    }
}