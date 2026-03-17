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
     * 生成 123456 的加密密码
     * 将输出的密文复制到数据库中使用
     */
    @Test
    void generatePasswordFor123456() {
        String rawPassword = "123456";
        String encodedPassword = passwordEncoder.encode(rawPassword);
        System.out.println("========================================");
        System.out.println("明文密码: " + rawPassword);
        System.out.println("加密密码: " + encodedPassword);
        System.out.println("========================================");
    }

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

    /**
     * 验证密码是否匹配
     * 用于验证生成的密码是否正确
     */
    @Test
    void verifyPassword() {
        String rawPassword = "123456";
        // 这里可以填入数据库中的加密密码进行验证
        String encodedPassword = "$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EO";

        boolean matches = passwordEncoder.matches(rawPassword, encodedPassword);
        System.out.println("========================================");
        System.out.println("明文密码: " + rawPassword);
        System.out.println("加密密码: " + encodedPassword);
        System.out.println("验证结果: " + (matches ? "匹配成功 ✓" : "匹配失败 ✗"));
        System.out.println("========================================");
    }

    /**
     * 批量生成多个测试用户的密码
     */
    @Test
    void generateMultiplePasswords() {
        String[] passwords = {"123456", "admin123", "test123", "password"};

        System.out.println("========================================");
        System.out.println("批量生成密码:");
        System.out.println("========================================");

        for (String pwd : passwords) {
            String encoded = passwordEncoder.encode(pwd);
            System.out.println("明文: " + pwd + " -> 密文: " + encoded);
        }
    }
}
