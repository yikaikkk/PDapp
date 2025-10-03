package com.app.backend.service.impl;

import com.alibaba.fastjson2.JSON;
import com.app.backend.entity.Like;
import com.app.backend.mapper.LikeMapper;
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
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Set;

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
    private RedisTemplate<String, Object> redisTemplate;
    
    @Override
    public void toggleLike(LikeVO likeVO) {

        //将消息投入到消息队列
        rabbitTemplate.convertAndSend(LIKE_EXCHANGE, "*", new Message(JSON.toJSONBytes(likeVO),new MessageProperties()));
    }


    @Override
    public void submitLikeMessage(LikeVO likeVO){
        // 先更新Redis缓存（使用Set保存点赞关系）
        try {
            String userLikeSetKey = "user:liked:articles:" + likeVO.getUserId();
            String articleLikedSetKey = "article:liked:users:" + likeVO.getArticleId();

            if(likeVO.getOperationType()==1) {
                // 将文章ID添加到用户点赞的文章集合中
                redisTemplate.opsForSet().add(userLikeSetKey, likeVO.getArticleId().toString());
                // 将用户ID添加到文章被点赞的用户集合中
                redisTemplate.opsForSet().add(articleLikedSetKey, likeVO.getUserId().toString());
                log.debug("已更新Redis缓存 - 用户{}点赞了文章{}, 已添加到Set中", likeVO.getUserId(), likeVO.getArticleId());
            }else {
                redisTemplate.opsForSet().remove(userLikeSetKey, likeVO.getArticleId().toString());
                // 将用户ID添加到文章被点赞的用户集合中
                redisTemplate.opsForSet().remove(articleLikedSetKey, likeVO.getUserId().toString());
                log.debug("已更新Redis缓存 - 用户{}取消点赞了文章{}, 已删除", likeVO.getUserId(), likeVO.getArticleId());

            }
        } catch (Exception e) {
            log.error("更新Redis点赞缓存失败", e);
        }
        
        // 再更新数据库
        LambdaQueryWrapper<Like> likeLambdaQueryWrapper=new LambdaQueryWrapper<>();
        likeLambdaQueryWrapper.eq(Like::getArticleId,likeVO.getArticleId());
        likeLambdaQueryWrapper.eq(Like::getUserId,likeVO.getUserId());

        Like like=this.getOne(likeLambdaQueryWrapper);
        if(like==null){
            Like likeToSave =new Like();
            likeToSave.setStatus(1);
            likeToSave.setArticleId(likeVO.getArticleId());
            likeToSave.setUserId(likeVO.getUserId());
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
        
        // 优先从Redis获取
        try {
            String articleLikedSetKey = "article:liked:users:" + articleId;
            Long size = redisTemplate.opsForSet().size(articleLikedSetKey);
            if (size != null) {
                return size.intValue();
            }
        } catch (Exception e) {
            log.warn("从Redis获取博文点赞数失败，fallback到数据库查询, articleId: {}", articleId, e);
        }
        
        // Redis未命中，从数据库查询
        return likeMapper.countLikesByArticleId(articleId);
    }
    
    @Override
    public Integer getLikeCountByUser(Long userId) {
        if (userId == null) {
            return 0;
        }
        
        // 优先从Redis获取
        try {
            String userLikeSetKey = "user:liked:articles:" + userId;
            Long size = redisTemplate.opsForSet().size(userLikeSetKey);
            if (size != null) {
                return size.intValue();
            }
        } catch (Exception e) {
            log.warn("从Redis获取用户点赞数失败，fallback到数据库查询, userId: {}", userId, e);
        }
        
        // Redis未命中，从数据库查询
        return likeMapper.countLikesByUserId(userId);
    }
    
    @Override
    @Transactional
    public boolean likeArticle(Long userId, Integer articleId) {
        if (userId == null || articleId == null) {
            return false;
        }
        
        // 先更新Redis缓存（使用Set保存点赞关系）
        try {
            String userLikeSetKey = "user:liked:articles:" + userId;
            String articleLikedSetKey = "article:liked:users:" + articleId;
            
            // 检查是否已经点赞
            Boolean isMember = redisTemplate.opsForSet().isMember(userLikeSetKey, articleId.toString());
            if (isMember != null && isMember) {
                // 已经点赞，抛出异常
                throw new RuntimeException("用户已点赞该文章");
            }
            
            // 将文章ID添加到用户点赞的文章集合中
            redisTemplate.opsForSet().add(userLikeSetKey, articleId.toString());
            // 将用户ID添加到文章被点赞的用户集合中
            redisTemplate.opsForSet().add(articleLikedSetKey, userId.toString());
            
            log.debug("已更新Redis缓存 - 用户{}点赞了文章{}, 已添加到Set中", userId, articleId);
        } catch (RuntimeException e) {
            log.error("用户已点赞该文章: userId={}, articleId={}", userId, articleId);
            throw e; // 重新抛出业务异常
        } catch (Exception e) {
            log.error("更新Redis点赞缓存失败", e);
        }
        
        // 再更新数据库
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
            // 检查是否已经点赞
            if (existingLike.getStatus() == 1) {
                // 已经点赞，抛出异常
                throw new RuntimeException("用户已点赞该文章");
            }
            
            // 更新现有记录状态
            UpdateWrapper<Like> updateWrapper = new UpdateWrapper<>();
            updateWrapper.eq("user_id", userId)
                        .eq("article_id", articleId)
                        .set("status", 1)
                        .set("update_time", LocalDateTime.now());
            
            int result = likeMapper.update(null, updateWrapper);
            success = result > 0;
        }
        
        return success;
    }
    
    @Override
    @Transactional
    public boolean unlikeArticle(Long userId, Integer articleId) {
        if (userId == null || articleId == null) {
            return false;
        }
        
        // 先更新Redis缓存
        try {
            String userLikeSetKey = "user:liked:articles:" + userId;
            String articleLikedSetKey = "article:liked:users:" + articleId;
            
            // 从用户点赞的文章集合中移除文章ID
            redisTemplate.opsForSet().remove(userLikeSetKey, articleId.toString());
            // 从文章被点赞的用户集合中移除用户ID
            redisTemplate.opsForSet().remove(articleLikedSetKey, userId.toString());
            
            log.debug("已更新Redis缓存 - 用户{}取消点赞文章{}, 已从Set中移除", userId, articleId);
        } catch (Exception e) {
            log.error("更新Redis取消点赞缓存失败", e);
        }
        
        // 再更新数据库
        UpdateWrapper<Like> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("user_id", userId)
                    .eq("article_id", articleId)
                    .set("status", 0)
                    .set("update_time", LocalDateTime.now());
        
        int result = likeMapper.update(null, updateWrapper);
        boolean success = result > 0;
        
        return success;
    }
}