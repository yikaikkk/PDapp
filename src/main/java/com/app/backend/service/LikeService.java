package com.app.backend.service;

import com.app.backend.vo.LikeVO;
import com.baomidou.mybatisplus.extension.service.IService;
import com.app.backend.entity.Like;

/**
 * 点赞服务接口
 */
public interface LikeService extends IService<Like> {

    void toggleLike(LikeVO likeVO);
    
    /**
     * 检查用户是否已点赞某博文
     * @param userId 用户ID
     * @param articleId 博文ID
     * @return 是否已点赞
     */
    boolean hasUserLikedArticle(Long userId, Integer articleId);
    
    /**
     * 获取博文的点赞数量
     * @param articleId 博文ID
     * @return 点赞数量
     */
    Integer getLikeCountByArticle(Integer articleId);
    
    /**
     * 获取用户的总点赞数
     * @param userId 用户ID
     * @return 总点赞数
     */
    Integer getLikeCountByUser(Long userId);
    
    /**
     * 用户点赞博文
     * @param userId 用户ID
     * @param articleId 博文ID
     * @return 是否成功
     */
    boolean likeArticle(Long userId, Integer articleId);
    
    /**
     * 用户取消点赞博文
     * @param userId 用户ID
     * @param articleId 博文ID
     * @return 是否成功
     */
    boolean unlikeArticle(Long userId, Integer articleId);


    /**
     * 处理消息队列中的点赞消息
     * @param likeVO
     */
    void submitLikeMessage(LikeVO likeVO);
}