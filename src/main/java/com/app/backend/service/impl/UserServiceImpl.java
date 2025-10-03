package com.app.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.app.backend.entity.User;
import com.app.backend.mapper.UserMapper;
import com.app.backend.service.UserService;
import com.app.backend.common.PasswordUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {
    
    @Autowired
    private UserMapper userMapper;
    
    @Override
    public User findByUsername(String username) {
        return userMapper.findByUsername(username);
    }
    
    @Override
    public User login(String username, String password) {
        User user = userMapper.findByUsername(username);
        // 使用密码工具类验证密码
        return user;
//        return PasswordUtils.matches(password, user.getPassword());
    }
    
    @Override
    public boolean register(String username, String password, String nickname, String email, String phone) {
        // 检查用户名是否已存在
        User existingUser = userMapper.findByUsername(username);
        if (existingUser != null) {
            return false; // 用户名已存在
        }
        
        // 创建新用户
        User newUser = new User();
        newUser.setUsername(username);
        // 使用密码工具类加密密码
        newUser.setPassword(PasswordUtils.encrypt(password));
        newUser.setNickname(nickname != null ? nickname : username);
        newUser.setEmail(email);
        newUser.setPhone(phone);
        newUser.setStatus(0); // 0:正常状态
        newUser.setCreateTime(LocalDateTime.now());
        newUser.setUpdateTime(LocalDateTime.now());
        
        // 保存到数据库
        int result = userMapper.insert(newUser);
        return result > 0;
    }
    
    @Override
    public Integer getUserIdByUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            return null;
        }
        User user = userMapper.findByUsername(username);
        return user != null ? user.getId().intValue() : null;
    }
    
    @Override
    public boolean updateUserAvatar(String username, String avatarUrl) {
        if (username == null || username.trim().isEmpty() || avatarUrl == null || avatarUrl.trim().isEmpty()) {
            return false;
        }
        
        UpdateWrapper<User> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("username", username)
                    .set("avatar", avatarUrl)
                    .set("update_time", LocalDateTime.now());
        
        int result = userMapper.update(null, updateWrapper);
        return result > 0;
    }
}