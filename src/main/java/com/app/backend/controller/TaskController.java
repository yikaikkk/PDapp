package com.app.backend.controller;

import com.app.backend.service.LikeCacheService;
import com.app.backend.task.LikeStatisticsJob;
import com.app.backend.vo.ResultVO;
import lombok.extern.slf4j.Slf4j;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 定时任务管理控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/admin/tasks")
public class TaskController {
    
    @Autowired
    private Scheduler scheduler;
    
    @Autowired
    private LikeCacheService likeCacheService;
    
    @Autowired
    private LikeStatisticsJob likeStatisticsJob;
    
    /**
     * 手动触发点赞统计任务
     */
    @PostMapping("/like-statistics/trigger")
    public ResultVO<String> triggerLikeStatistics() {
        try {
            // 手动执行任务
            likeStatisticsJob.execute(null);
            return ResultVO.ok("点赞统计任务手动触发成功");
        } catch (Exception e) {
            log.error("手动触发点赞统计任务失败", e);
            return ResultVO.fail("任务执行失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取定时任务状态
     */
    @GetMapping("/like-statistics/status")
    public ResultVO<Map<String, Object>> getLikeStatisticsTaskStatus() {
        try {
            Map<String, Object> status = new HashMap<>();
            
            // 获取任务状态
            JobKey jobKey = new JobKey("likeStatisticsJob", "statisticsGroup");
            boolean exists = scheduler.checkExists(jobKey);
            
            status.put("taskExists", exists);
            status.put("taskName", "likeStatisticsJob");
            status.put("taskGroup", "statisticsGroup");
            
            if (exists) {
                status.put("jobDetail", scheduler.getJobDetail(jobKey));
                status.put("triggers", scheduler.getTriggersOfJob(jobKey));
            }
            
            return ResultVO.ok(status);
        } catch (Exception e) {
            log.error("获取任务状态失败", e);
            return ResultVO.fail("获取任务状态失败: " + e.getMessage());
        }
    }
    
    /**
     * 暂停点赞统计任务
     */
    @PostMapping("/like-statistics/pause")
    public ResultVO<String> pauseLikeStatisticsTask() {
        try {
            JobKey jobKey = new JobKey("likeStatisticsJob", "statisticsGroup");
            scheduler.pauseJob(jobKey);
            return ResultVO.ok("点赞统计任务已暂停");
        } catch (Exception e) {
            log.error("暂停任务失败", e);
            return ResultVO.fail("暂停任务失败: " + e.getMessage());
        }
    }
    
    /**
     * 恢复点赞统计任务
     */
    @PostMapping("/like-statistics/resume")
    public ResultVO<String> resumeLikeStatisticsTask() {
        try {
            JobKey jobKey = new JobKey("likeStatisticsJob", "statisticsGroup");
            scheduler.resumeJob(jobKey);
            return ResultVO.ok("点赞统计任务已恢复");
        } catch (Exception e) {
            log.error("恢复任务失败", e);
            return ResultVO.fail("恢复任务失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取缓存统计信息
     */
    @GetMapping("/cache/statistics")
    public ResultVO<Map<String, Object>> getCacheStatistics() {
        try {
            Map<String, Object> statistics = likeCacheService.getCacheStatistics();
            return ResultVO.ok(statistics);
        } catch (Exception e) {
            log.error("获取缓存统计信息失败", e);
            return ResultVO.fail("获取缓存统计信息失败: " + e.getMessage());
        }
    }
    
    /**
     * 清空所有点赞缓存
     */
    @DeleteMapping("/cache/clear")
    public ResultVO<String> clearAllLikeCaches() {
        try {
            likeCacheService.clearAllLikeCaches();
            return ResultVO.ok("所有点赞缓存已清空");
        } catch (Exception e) {
            log.error("清空缓存失败", e);
            return ResultVO.fail("清空缓存失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取特定用户的点赞数量（从缓存）
     */
    @GetMapping("/cache/user/{userId}/likes")
    public ResultVO<Integer> getUserLikeCountFromCache(@PathVariable Long userId) {
        try {
            Integer likeCount = likeCacheService.getUserLikeCount(userId);
            return ResultVO.ok(likeCount);
        } catch (Exception e) {
            log.error("获取用户点赞缓存失败, userId: {}", userId, e);
            return ResultVO.fail("获取用户点赞缓存失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取特定博文的点赞数量（从缓存）
     */
    @GetMapping("/cache/article/{articleId}/likes")
    public ResultVO<Integer> getArticleLikeCountFromCache(@PathVariable Integer articleId) {
        try {
            Integer likeCount = likeCacheService.getArticleLikeCount(articleId);
            return ResultVO.ok(likeCount);
        } catch (Exception e) {
            log.error("获取博文点赞缓存失败, articleId: {}", articleId, e);
            return ResultVO.fail("获取博文点赞缓存失败: " + e.getMessage());
        }
    }
}