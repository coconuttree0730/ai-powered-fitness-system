package com.fitness.common.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {

    private String secret;

    /**
     * Access Token 过期时间（毫秒），默认 2 小时
     */
    private Long accessExpiration = 7200000L;

    /**
     * Refresh Token 过期时间（毫秒），默认 7 天
     */
    private Long refreshExpiration = 604800000L;

    /**
     * 勾选"记住我"后 Refresh Token 过期时间（毫秒），默认 30 天
     */
    private Long rememberMeExpiration = 2592000000L;

    private String issuer;

    private String audience;
}
