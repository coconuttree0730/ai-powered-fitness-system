package com.fitness.common.utils;

import com.fitness.common.config.JwtProperties;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtUtils {

    private final JwtProperties jwtProperties;

    /**
     * 获取签名密钥 ：生成签名密钥，用于签名JWT Token
     * @return 签名密钥
     *
     */
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 生成JWT Token 单参数方案
     * @param username 用户名
     * @return JWT Token
     */
    public String generateToken(String username) {
        //调用有参数的生成方法
        return generateToken(username, new HashMap<>());
    }

    /**
     * 生成JWT Token 多参数方案
     * @param username 用户名
     * @param claims 自定义声明
     * @return JWT Token
     */
    public String generateToken(String username, Map<String, Object> claims) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtProperties.getExpiration());

        return Jwts.builder()
                .claims(claims)
                .subject(username)
                .issuedAt(now)
                .expiration(expiryDate)
                .issuer(jwtProperties.getIssuer())
                .audience().add(jwtProperties.getAudience()).and()
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * 解析Token的打包数据（例如用户id号）;claims:map打包
     * @param token JWT Token
     * @return 用户名
     */
    public String getUsernameFromToken(String token) {
        Claims claims = parseToken(token);
        return claims != null ? claims.getSubject() : null;
    }

    /**
     * 解析Token-校验合法性
     * @param token JWT Token
     * @return 用户名
     */
    public boolean validateToken(String token) {
        try {
            parseToken(token);
            return true;
        } catch (ExpiredJwtException e) {
            log.warn("JWT Token已过期");
        } catch (UnsupportedJwtException e) {
            log.warn("不支持的JWT Token");
        } catch (MalformedJwtException e) {
            log.warn("JWT Token格式错误");
        } catch (SecurityException e) {
            log.warn("JWT Token签名验证失败");
        } catch (IllegalArgumentException e) {
            log.warn("JWT Token为空或非法");
        }
        return false;
    }

    /**
     * 判断Token是否已过期 (过期时间和当前时间对比)
     * @param token JWT Token
     * @return true-已过期
     */
    public boolean isTokenExpired(String token) {
        try {
            Claims claims = parseToken(token);
            return claims.getExpiration().before(new Date());
        } catch (ExpiredJwtException e) {
            return true;
        }
    }

    /* *
     * __解析Token__ 获取Claims（打包数据，包含 用户id）
     */
    public Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * 获取Token过期时间
     * @return 过期时间（毫秒）
     */
    public long getExpirationTime() {
        return jwtProperties.getExpiration();
    }

    /**
     * 获取刷新Token过期时间
     * @return 刷新过期时间（毫秒）
     */
    public long getRefreshExpirationTime() {
        return jwtProperties.getRefreshExpiration();
    }

}
