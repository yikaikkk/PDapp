package com.app.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.app.backend.entity.Collect;
import org.apache.ibatis.annotations.Mapper;

/**
 * 收藏Mapper接口
 */
@Mapper
public interface CollectMapper extends BaseMapper<Collect> {
    // 可以添加自定义的SQL查询方法
}