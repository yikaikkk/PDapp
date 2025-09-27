package com.app.backend.service.impl;

import com.alibaba.fastjson2.JSON;
import com.app.backend.entity.Like;
import com.app.backend.mapper.LikeMapper;
import com.app.backend.service.LikeCacheService;
import com.app.backend.service.LikeService;
import com.app.backend.vo.LikeVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static com.app.backend.constant.RabbitMQConstant.LIKE_EXCHANGE;

/**
 * 点赞服务实现类
 */
@Slf4j
@Service
public class LikeServiceImpl extends ServiceImpl<LikeMapper, Like> implements LikeService {
    
    @Autowired
    private LikeMapper likeMapper;

    @Autowired
    private RabbitTemplate rabbitTemplate;
    
    @Autowired
    private LikeCacheService likeCacheService;
    
    @Override
    public void toggleLike(LikeVO likeVO) {

        //将消息投入到消息队列
        rabbitTemplate.convertAndSend(LIKE_EXCHANGE, "*", new Message(JSON.toJSONBytes(likeVO),new MessageProperties()));
    }


    @Override
    public void submitLikeMessage(LikeVO likeVO){
        //检查是否存在该点赞
        LambdaQueryWrapper<Like> likeLambdaQueryWrapper=new LambdaQueryWrapper<>();
        likeLambdaQueryWrapper.eq(Like::getArticleId,likeVO.getArticleId());
        likeLambdaQueryWrapper.eq(Like::getUserId,likeVO.getUerId());

        Like like=this.getOne(likeLambdaQueryWrapper);
        if(like==null){
            Like likeToSave =new Like();
            likeToSave.setStatus(1);
            likeToSave.setArticleId(likeVO.getArticleId());
            likeToSave.setUserId(likeVO.getUerId());
            likeToSave.setCreateTime(LocalDateTime.now());
            likeToSave.setUpdateTime(LocalDateTime.now());
            this.save(likeToSave);
        }else{
            if( like.getStatus()==0){
                like.setStatus(1);
                like.setUpdateTime(LocalDateTime.now());
                this.updateById(like);
            }
        }
        
        // 更新缓存
        try {
            Integer userLikeCount = getLikeCountByUser(likeVO.getUerId());
            Integer articleLikeCount = getLikeCountByArticle(likeVO.getArticleId());
            
            likeCacheService.updateUserLikeCount(likeVO.getUerId(), userLikeCount);
            likeCacheService.updateArticleLikeCount(likeVO.getArticleId(), articleLikeCount);
            
            log.debug("已更新缓存 - 用户{}: {}点赞, 博文{}: {}点赞", 
                    likeVO.getUerId(), userLikeCount, likeVO.getArticleId(), articleLikeCount);
        } catch (Exception e) {
            log.error("更新点赞缓存失败", e);
        }
    }



    @Override
    public boolean hasUserLikedArticle(Long userId, Integer articleId) {
        if (userId == null || articleId == null) {
            return false;
        }
        
        Like like = likeMapper.findByUserIdAndArticleId(userId, articleId);
        return like != null && like.getStatus() == 1;
    }
    
    @Override
    public Integer getLikeCountByArticle(Integer articleId) {
        if (articleId == null) {
            return 0;
        }
        
        // 优先从缓存获取
        try {
            Integer cachedCount = likeCacheService.getArticleLikeCount(articleId);
            if (cachedCount != null && cachedCount >= 0) {
                return cachedCount;
            }
        } catch (Exception e) {
            log.warn("从缓存获取博文点赞数失败，fallback到数据库查询, articleId: {}", articleId, e);
        }
        
        // 缓存未命中，从数据库查询
        return likeMapper.countLikesByArticleId(articleId);
    }
    
    @Override
    public Integer getLikeCountByUser(Long userId) {
        if (userId == null) {
            return 0;
        }
        
        // 优先从缓存获取
        try {
            Integer cachedCount = likeCacheService.getUserLikeCount(userId);
            if (cachedCount != null && cachedCount >= 0) {
                return cachedCount;
            }
        } catch (Exception e) {
            log.warn("从缓存获取用户点赞数失败，fallback到数据库查询, userId: {}", userId, e);
        }
        
        // 缓存未命中，从数据库查询
        return likeMapper.countLikesByUserId(userId);
    }
    
    @Override
    @Transactional
    public boolean likeArticle(Long userId, Integer articleId) {
        if (userId == null || articleId == null) {
            return false;
        }
        
        boolean success = false;
        
        // 查询现有记录
        Like existingLike = likeMapper.findByUserIdAndArticleId(userId, articleId);
        
        if (existingLike == null) {
            // 创建新记录
            Like newLike = new Like();
            newLike.setUserId(userId);
            newLike.setArticleId(articleId);
            newLike.setStatus(1);
            newLike.setCreateTime(LocalDateTime.now());
            newLike.setUpdateTime(LocalDateTime.now());
            
            int result = likeMapper.insert(newLike);
            success = result > 0;
        } else {
            // 更新现有记录状态
            UpdateWrapper<Like> updateWrapper = new UpdateWrapper<>();
            updateWrapper.eq("user_id", userId)
                        .eq("article_id", articleId)
                        .set("status", 1)
                        .set("update_time", LocalDateTime.now());
            
            int result = likeMapper.update(null, updateWrapper);
            success = result > 0;
        }
        
        // 如果操作成功，更新缓存
        if (success) {
            try {
                Integer userLikeCount = likeMapper.countLikesByUserId(userId);
                Integer articleLikeCount = likeMapper.countLikesByArticleId(articleId);
                
                likeCacheService.updateUserLikeCount(userId, userLikeCount);
                likeCacheService.updateArticleLikeCount(articleId, articleLikeCount);
                
                log.debug("点赞成功并更新缓存 - 用户{}: {}点赞, 博文{}: {}点赞", 
                        userId, userLikeCount, articleId, articleLikeCount);
            } catch (Exception e) {
                log.error("点赞后更新缓存失败", e);
            }
        }
        
        return success;
    }
    
    @Override
    @Transactional
    public boolean unlikeArticle(Long userId, Integer articleId) {
        if (userId == null || articleId == null) {
            return false;
        }
        
        UpdateWrapper<Like> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("user_id", userId)
                    .eq("article_id", articleId)
                    .set("status", 0)
                    .set("update_time", LocalDateTime.now());
        
        int result = likeMapper.update(null, updateWrapper);
        boolean success = result > 0;
        
        // 如果操作成功，更新缓存
        if (success) {
            try {
                Integer userLikeCount = likeMapper.countLikesByUserId(userId);
                Integer articleLikeCount = likeMapper.countLikesByArticleId(articleId);
                
                likeCacheService.updateUserLikeCount(userId, userLikeCount);
                likeCacheService.updateArticleLikeCount(articleId, articleLikeCount);
                
                log.debug("取消点赞成功并更新缓存 - 用户{}: {}点赞, 博文{}: {}点赞", 
                        userId, userLikeCount, articleId, articleLikeCount);
            } catch (Exception e) {
                log.error("取消点赞后更新缓存失败", e);
            }
        }
        
        return success;
    }
}