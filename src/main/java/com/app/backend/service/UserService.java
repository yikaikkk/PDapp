package com.app.backend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.app.backend.entity.User;

public interface UserService extends IService<User> {
    User findByUsername(String username);
    User login(String username, String password);
    boolean register(String username, String password, String nickname, String email, String phone);
    
    /**
     * 根据username获取用户ID
     * @param username 用户名
     * @return 用户ID，如果用户不存在则返回null
     */
    Integer getUserIdByUsername(String username);
    
    /**
     * 更新用户头像
     * @param username 用户名
     * @param avatarUrl 头像URL
     * @return 是否更新成功
     */
    boolean updateUserAvatar(String username, String avatarUrl);
}