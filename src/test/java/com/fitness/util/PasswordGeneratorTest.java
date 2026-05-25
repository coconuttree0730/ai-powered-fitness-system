package com.fitness.util;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * BCrypt 密码生成工具测试类
 * 用于生成加密密码，方便测试登录功能
 */
public class PasswordGeneratorTest {

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    /**
     * 生成自定义密码的加密字符串
     * 修改 rawPassword 变量即可生成不同密码
     */
    @Test
    void generateCustomPassword() {
        String rawPassword = "admin123";  // 修改这里生成其他密码
        String encodedPassword = passwordEncoder.encode(rawPassword);
        System.out.println("========================================");
        System.out.println("明文密码: " + rawPassword);
        System.out.println("加密密码: " + encodedPassword);
        System.out.println("========================================");
    }

}
