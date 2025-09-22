package com.app.backend.common;

import org.springframework.util.DigestUtils;

/**
 * 密码加密工具类
 */
public class PasswordUtils {
    
    /**
     * MD5加密密码
     * @param password 原始密码
     * @return 加密后的密码
     */
    public static String encrypt(String password) {
        if (password == null || password.trim().isEmpty()) {
            throw new IllegalArgumentException("密码不能为空");
        }
        return DigestUtils.md5DigestAsHex(password.getBytes());
    }
    
    /**
     * 验证密码是否匹配
     * @param rawPassword 原始密码
     * @param encryptedPassword 加密后的密码
     * @return 是否匹配
     */
    public static boolean matches(String rawPassword, String encryptedPassword) {
        if (rawPassword == null || encryptedPassword == null) {
            return false;
        }
        return encrypt(rawPassword).equals(encryptedPassword);
    }
}