package com.app.backend.controller;

import com.app.backend.common.JwtUtils;
import com.app.backend.common.ValidationUtils;
import com.app.backend.exception.UnauthorizedException;
import com.app.backend.service.UserService;
import com.app.backend.vo.LoginVO;
import com.app.backend.vo.RegisterVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class UserController {
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private JwtUtils jwtUtils;
    
    @PostMapping("/register")
    public Map<String, Object> register(@RequestBody RegisterVO registerRequest) {
//        String username = registerRequest.get("username");
//        String password = registerRequest.get("password");
//        String nickname = registerRequest.get("nickname");
//        String email = registerRequest.get("email");
//        String phone = registerRequest.get("phone");
        
        // 验证用户名
        if (!ValidationUtils.isValidUsername(registerRequest.getUsername())) {
            throw new UnauthorizedException("用户名格式不正确，应为4-20位字母、数字或下划线");
        }
        
        // 验证密码强度
        if (!ValidationUtils.isValidPassword(registerRequest.getPassword())) {
            throw new UnauthorizedException("密码强度不够，应为至少6位且包含字母和数字");
        }
        
        // 验证邮箱格式（如果提供）
        if (registerRequest.getEmail() != null && !registerRequest.getEmail().trim().isEmpty() && !ValidationUtils.isValidEmail(registerRequest.getEmail())) {
            throw new UnauthorizedException("邮箱格式不正确");
        }
        
        // 验证手机号格式（如果提供）
        if (registerRequest.getPhone() != null && !registerRequest.getPhone().trim().isEmpty() && !ValidationUtils.isValidPhone(registerRequest.getPhone())) {
            throw new UnauthorizedException("手机号格式不正确");
        }
        
        try {
            boolean isRegistered = userService.register(registerRequest.getUsername(), registerRequest.getPassword(), registerRequest.getNickname(), registerRequest.getEmail(), registerRequest.getPhone());
            if (!isRegistered) {
                throw new UnauthorizedException("注册失败，用户名可能已存在");
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "注册成功");
            response.put("username", registerRequest.getUsername());
            
            return response;
        } catch (Exception e) {
            throw new UnauthorizedException("注册失败: " + e.getMessage());
        }
    }
    
    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody LoginVO loginRequest) {
//        String username = loginRequest.get("username");
//        String password = loginRequest.get("password");
        
        if (loginRequest.getUsername() == null || loginRequest.getPassword() == null) {
            throw new UnauthorizedException("用户名或密码不能为空");
        }
        
        boolean isAuthenticated = userService.login(loginRequest.getUsername(), loginRequest.getPassword());
        if (!isAuthenticated) {
            throw new UnauthorizedException("用户名或密码错误");
        }
        
        // 生成token
        String token = jwtUtils.generateToken(loginRequest.getUsername());
        
        Map<String, Object> response = new HashMap<>();
        response.put("token", token);
        response.put("message", "登录成功");
        
        return response;
    }
    
    @GetMapping("/userInfo")
    public Map<String, Object> getUserInfo(@RequestAttribute("username") String username) {
        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("username", username);
        userInfo.put("role", "user"); // 实际项目中应该从数据库获取用户角色
        return userInfo;
    }
}