package com.fitness.common.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * JWT 配置属性
 * 从 application.yml 读取 jwt 前缀的配置
 */
@Data
@Component
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {

    /**
     * JWT 密钥
     */
    private String secret;

    /**
     * Token 过期时间（毫秒）
     */
    private Long expiration;

    /**
     * 刷新 Token 过期时间（毫秒）
     */
    private Long refreshExpiration;

    /**
     * 签发者
     */
    private String issuer;

    /**
     * 受众
     */
    private String audience;
}
