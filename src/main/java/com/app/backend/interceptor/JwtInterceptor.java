package com.app.backend.interceptor;

import com.app.backend.common.JwtUtils;
import com.app.backend.exception.UnauthorizedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtInterceptor implements HandlerInterceptor {
    
    @Autowired
    private JwtUtils jwtUtils;
    
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 允许OPTIONS请求通过
        if (request.getMethod().equals("OPTIONS")) {
            return true;
        }
        
        // 从请求头中获取token
        String token = request.getHeader("Authorization");
        
        // 如果不是映射到方法直接通过
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }
        
        // 如果token为空或格式不正确
        if (token == null || !token.startsWith("Bearer ")) {
            throw new UnauthorizedException("未提供有效的token");
        }
        
        // 截取token值
        token = token.substring(7);
        
        try {
            // 验证token
            String username = jwtUtils.getUsernameFromToken(token);
            if (username == null || jwtUtils.isTokenExpired(token)) {
                throw new UnauthorizedException("token已过期或无效");
            }
            
            // 将用户名放入请求属性中，供后续处理使用
            request.setAttribute("username", username);
            
            // 验证token是否有效（可选的额外验证）
            if (!jwtUtils.validateToken(token, username)) {
                throw new UnauthorizedException("token验证失败");
            }
            
        } catch (UnauthorizedException e) {
            throw e; // 重新抛出自定义异常
        } catch (Exception e) {
            throw new UnauthorizedException("token验证失败: " + e.getMessage());
        }
        
        return true;
    }
    
    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        // 不需要实现
    }
    
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        // 不需要实现
    }
}