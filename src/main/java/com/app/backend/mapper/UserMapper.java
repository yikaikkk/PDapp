package com.app.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.app.backend.entity.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<User> {
    // 可以添加自定义的SQL查询方法
    User findByUsername(String username);
}