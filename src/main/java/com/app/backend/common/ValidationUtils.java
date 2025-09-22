package com.app.backend.common;

import java.util.regex.Pattern;

public class ValidationUtils {
    
    // 邮箱验证正则表达式
    private static final Pattern EMAIL_PATTERN = 
        Pattern.compile("^[A-Za-z0-9+_.-]+@([A-Za-z0-9.-]+\\.[A-Za-z]{2,})$");
    
    // 手机号验证正则表达式（简单版本）
    private static final Pattern PHONE_PATTERN = 
        Pattern.compile("^1[3-9]\\d{9}$");
    
    // 用户名验证正则表达式（字母、数字、下划线，4-20位）
    private static final Pattern USERNAME_PATTERN = 
        Pattern.compile("^[a-zA-Z0-9_]{4,20}$");
    
    /**
     * 验证邮箱格式
     */
    public static boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return true; // 邮箱可以为空
        }
        return EMAIL_PATTERN.matcher(email.trim()).matches();
    }
    
    /**
     * 验证手机号格式
     */
    public static boolean isValidPhone(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            return true; // 手机号可以为空
        }
        return PHONE_PATTERN.matcher(phone.trim()).matches();
    }
    
    /**
     * 验证用户名格式
     */
    public static boolean isValidUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            return false;
        }
        return USERNAME_PATTERN.matcher(username.trim()).matches();
    }
    
    /**
     * 验证密码强度（至少6位，包含字母和数字）
     */
    public static boolean isValidPassword(String password) {
        if (password == null || password.length() < 6) {
            return false;
        }
        // 检查是否包含字母和数字
        boolean hasLetter = password.matches(".*[a-zA-Z].*");
        boolean hasDigit = password.matches(".*\\d.*");
        return hasLetter && hasDigit;
    }
}