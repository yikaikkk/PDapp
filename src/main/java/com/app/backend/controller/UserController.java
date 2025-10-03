package com.app.backend.controller;

import com.app.backend.common.JwtUtils;
import com.app.backend.common.PasswordUtils;
import com.app.backend.common.ValidationUtils;
import com.app.backend.entity.User;
import com.app.backend.enums.FilePathEnum;
import com.app.backend.exception.UnauthorizedException;
import com.app.backend.service.UserService;
import com.app.backend.strategy.context.UploadStrategyContext;
import com.app.backend.vo.LoginVO;
import com.app.backend.vo.RegisterVO;
import com.app.backend.vo.ResultVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class UserController {
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private JwtUtils jwtUtils;
    
    @Autowired
    private UploadStrategyContext uploadStrategyContext;
    
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
        
        User isAuthenticatedUser = userService.login(loginRequest.getUsername(), loginRequest.getPassword());
        if (isAuthenticatedUser==null||!PasswordUtils.matches(loginRequest.getPassword(), isAuthenticatedUser.getPassword())) {
            throw new UnauthorizedException("用户名或密码错误");
        }
        
        // 生成token
        String token = jwtUtils.generateToken(loginRequest.getUsername());
        
        Map<String, Object> response = new HashMap<>();
        response.put("id",isAuthenticatedUser.getId());
        response.put("userName",isAuthenticatedUser.getNickname());
        response.put("avtar",isAuthenticatedUser.getAvatar());
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
    
    /**
     * 上传用户头像
     */
    @PostMapping("/avatar")
    public ResultVO<String> uploadAvatar(MultipartFile file, @RequestAttribute("username") String username) {
        if (file == null || file.isEmpty()) {
            return ResultVO.fail("请选择要上传的头像文件");
        }
        
        // 验证文件类型
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            return ResultVO.fail("只能上传图片文件");
        }
        
        // 验证文件大小（限制5MB）
        if (file.getSize() > 5 * 1024 * 1024) {
            return ResultVO.fail("头像文件大小不能超过5MB");
        }
        
        try {
            // 上传头像到文件服务器
            String avatarUrl = uploadStrategyContext.executeUploadStrategy(file, FilePathEnum.AVATAR.getPath());
            
            // 更新用户头像URL到数据库
            boolean updateSuccess = userService.updateUserAvatar(username, avatarUrl);
            if (!updateSuccess) {
                return ResultVO.fail("头像上传失败，请稍后重试");
            }
            
            return ResultVO.ok(avatarUrl, "头像上传成功");
        } catch (Exception e) {
            return ResultVO.fail("头像上传失败: " + e.getMessage());
        }
    }
}