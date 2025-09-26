package com.app.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.app.backend.entity.Like;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 点赞Mapper接口
 */
@Mapper
public interface LikeMapper extends BaseMapper<Like> {
    
    /**
     * 查询用户对特定博文的点赞记录
     * @param userId 用户ID
     * @param articleId 博文ID
     * @return 点赞记录
     */
    @Select("SELECT * FROM pd_like WHERE user_id = #{userId} AND article_id = #{articleId}")
    Like findByUserIdAndArticleId(@Param("userId") Long userId, @Param("articleId") Integer articleId);
    
    /**
     * 统计博文的点赞数量
     * @param articleId 博文ID
     * @return 点赞数量
     */
    @Select("SELECT COUNT(*) FROM pd_like WHERE article_id = #{articleId} AND status = 1")
    Integer countLikesByArticleId(@Param("articleId") Integer articleId);
    
    /**
     * 统计用户的总点赞数
     * @param userId 用户ID
     * @return 总点赞数
     */
    @Select("SELECT COUNT(*) FROM pd_like WHERE user_id = #{userId} AND status = 1")
    Integer countLikesByUserId(@Param("userId") Long userId);
}