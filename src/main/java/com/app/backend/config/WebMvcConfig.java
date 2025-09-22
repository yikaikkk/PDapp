package com.app.backend.config;

import com.app.backend.interceptor.JwtInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
    
    @Autowired
    private JwtInterceptor jwtInterceptor;
    
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 注册JWT拦截器
        registry.addInterceptor(jwtInterceptor)
                // 拦截所有请求
                .addPathPatterns("/**")
                // 排除登录和注册接口
                .excludePathPatterns("/api/auth/login", "/api/auth/register")
                // 排除静态资源
                .excludePathPatterns("/static/**")
                // 排除swagger接口
                .excludePathPatterns("/swagger-resources/**", "/webjars/**", "/*swagger*/**", "/v2/api-docs")
                // 排除error接口
                .excludePathPatterns("/error");
    }
}