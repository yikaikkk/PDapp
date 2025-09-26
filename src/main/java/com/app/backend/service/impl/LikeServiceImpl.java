package com.app.backend.service.impl;

import com.alibaba.fastjson2.JSON;
import com.app.backend.entity.Like;
import com.app.backend.mapper.LikeMapper;
import com.app.backend.service.LikeService;
import com.app.backend.vo.LikeVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
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
@Service
public class LikeServiceImpl extends ServiceImpl<LikeMapper, Like> implements LikeService {
    
    @Autowired
    private LikeMapper likeMapper;

    @Autowired
    private RabbitTemplate rabbitTemplate;
    
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
        }else{
            if( like.getStatus()==0){
                like.setStatus(1);
            }
            this.updateById(like);
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
        return likeMapper.countLikesByArticleId(articleId);
    }
    
    @Override
    public Integer getLikeCountByUser(Long userId) {
        if (userId == null) {
            return 0;
        }
        return likeMapper.countLikesByUserId(userId);
    }
    
    @Override
    @Transactional
    public boolean likeArticle(Long userId, Integer articleId) {
        if (userId == null || articleId == null) {
            return false;
        }
        
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
            return result > 0;
        } else {
            // 更新现有记录状态
            UpdateWrapper<Like> updateWrapper = new UpdateWrapper<>();
            updateWrapper.eq("user_id", userId)
                        .eq("article_id", articleId)
                        .set("status", 1)
                        .set("update_time", LocalDateTime.now());
            
            int result = likeMapper.update(null, updateWrapper);
            return result > 0;
        }
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
        return result > 0;
    }
}