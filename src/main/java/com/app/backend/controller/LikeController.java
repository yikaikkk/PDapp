package com.app.backend.controller;

import com.app.backend.service.LikeService;
import com.app.backend.service.UserService;
import com.app.backend.vo.LikeVO;
import com.app.backend.vo.ResultVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 点赞控制器
 */
@RestController
@RequestMapping("/api/likes")
public class LikeController {
    
    @Autowired
    private LikeService likeService;
    
    @Autowired
    private UserService userService;
    
    /**
     * 获取当前用户ID
     * @param username 从JWT token中解析出的用户名
     * @return 用户ID
     */
    private Long getCurrentUserId(String username) {
        Integer userId = userService.getUserIdByUsername(username);
        if (userId == null) {
            throw new RuntimeException("用户不存在");
        }
        return userId.longValue();
    }
    
    /**
     * 切换点赞状态（点赞/取消点赞）
     */
    @PostMapping("/toggle/like")
    public ResultVO<?> toggleLike(@RequestBody LikeVO likeVO,
                                                    @RequestAttribute("username") String username) {
        try {
            // 设置用户ID
            Long userId = getCurrentUserId(username);
            likeVO.setUserId(userId);
            
            likeService.toggleLike(likeVO);
        } catch (Exception e) {
            return ResultVO.fail("操作失败: " + e.getMessage());
        }
        return ResultVO.ok();
    }
    
    /**
     * 检查用户是否已点赞某博文
     */
    @GetMapping("/status/{articleId}")
    public ResultVO<Map<String, Object>> getLikeStatus(@PathVariable Integer articleId,
                                                      @RequestAttribute("username") String username) {
        try {
            Long userId = getCurrentUserId(username);
            
            boolean isLiked = likeService.hasUserLikedArticle(userId, articleId);
            Integer likeCount = likeService.getLikeCountByArticle(articleId);
            
            Map<String, Object> result = new HashMap<>();
            result.put("isLiked", isLiked);
            result.put("likeCount", likeCount);
            
            return ResultVO.ok(result);
        } catch (Exception e) {
            return ResultVO.fail("查询失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取博文的点赞数量
     */
    @GetMapping("/count/{articleId}")
    public ResultVO<Integer> getLikeCount(@PathVariable Integer articleId) {
        try {
            Integer likeCount = likeService.getLikeCountByArticle(articleId);
            return ResultVO.ok(likeCount);
        } catch (Exception e) {
            return ResultVO.fail("查询失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取用户的总点赞数
     */
    @GetMapping("/user/count")
    public ResultVO<Integer> getUserLikeCount(@RequestAttribute("username") String username) {
        try {
            Long userId = getCurrentUserId(username);
            Integer likeCount = likeService.getLikeCountByUser(userId);
            return ResultVO.ok(likeCount);
        } catch (Exception e) {
            return ResultVO.fail("查询失败: " + e.getMessage());
        }
    }
    
    /**
     * 用户点赞博文
     */
    @PostMapping("/{articleId}")
    public ResultVO<Map<String, Object>> likeArticle(@PathVariable Integer articleId,
                                                    @RequestAttribute("username") String username) {
        try {
            Long userId = getCurrentUserId(username);
            
            boolean success = likeService.likeArticle(userId, articleId);
            if (success) {
                Integer likeCount = likeService.getLikeCountByArticle(articleId);
                
                Map<String, Object> result = new HashMap<>();
                result.put("isLiked", true);
                result.put("likeCount", likeCount);
                
                return ResultVO.ok(result, "点赞成功");
            } else {
                return ResultVO.fail("点赞失败");
            }
        } catch (Exception e) {
            return ResultVO.fail("点赞失败: " + e.getMessage());
        }
    }
    
    /**
     * 用户取消点赞博文
     */
    @DeleteMapping("/{articleId}")
    public ResultVO<Map<String, Object>> unlikeArticle(@PathVariable Integer articleId,
                                                      @RequestAttribute("username") String username) {
        try {
            Long userId = getCurrentUserId(username);
            
            boolean success = likeService.unlikeArticle(userId, articleId);
            if (success) {
                Integer likeCount = likeService.getLikeCountByArticle(articleId);
                
                Map<String, Object> result = new HashMap<>();
                result.put("isLiked", false);
                result.put("likeCount", likeCount);
                
                return ResultVO.ok(result, "取消点赞成功");
            } else {
                return ResultVO.fail("取消点赞失败");
            }
        } catch (Exception e) {
            return ResultVO.fail("取消点赞失败: " + e.getMessage());
        }
    }
}