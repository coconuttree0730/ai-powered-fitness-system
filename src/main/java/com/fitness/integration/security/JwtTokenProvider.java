package com.fitness.integration.security;

import com.fitness.common.config.JwtProperties;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtTokenProvider {

    private final JwtProperties jwtProperties;

    private static final String CLAIM_USER_ID = "userId";
    private static final String CLAIM_ROLES = "roles";
    private static final String CLAIM_USERNAME = "username";
    private static final String CLAIM_TOKEN_TYPE = "type";
    private static final String TOKEN_TYPE_ACCESS = "access";
    private static final String TOKEN_TYPE_REFRESH = "refresh";

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8));
    }

    public String generateToken(Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        return generateAccessToken(userDetails.getId(), userDetails.getUsername(),
                userDetails.getAuthorities().stream()
                        .map(GrantedAuthority::getAuthority)
                        .collect(Collectors.toList()));
    }

    public String generateToken(UserDetailsImpl userDetails) {
        return generateAccessToken(userDetails.getId(), userDetails.getUsername(),
                userDetails.getAuthorities().stream()
                        .map(GrantedAuthority::getAuthority)
                        .collect(Collectors.toList()));
    }

    public String generateToken(Long userId, String username, List<String> roles) {
        return generateAccessToken(userId, username, roles);
    }

    /**
     * 生成 Access Token（短期，用于API请求认证）
     */
    public String generateAccessToken(Long userId, String username, List<String> roles) {
        return buildToken(userId, username, roles, TOKEN_TYPE_ACCESS, jwtProperties.getAccessExpiration());
    }

    /**
     * 生成 Refresh Token（长期，用于刷新Access Token）
     */
    public String generateRefreshToken(Long userId, String username, List<String> roles) {
        return buildToken(userId, username, roles, TOKEN_TYPE_REFRESH, jwtProperties.getRefreshExpiration());
    }

    /**
     * 生成"记住我"的 Refresh Token（超长期）
     */
    public String generateRememberMeRefreshToken(Long userId, String username, List<String> roles) {
        return buildToken(userId, username, roles, TOKEN_TYPE_REFRESH, jwtProperties.getRememberMeExpiration());
    }

    private String buildToken(Long userId, String username, List<String> roles, String tokenType, Long expirationMs) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expirationMs);

        Map<String, Object> claims = new HashMap<>();
        claims.put(CLAIM_USER_ID, userId);
        claims.put(CLAIM_USERNAME, username);
        claims.put(CLAIM_ROLES, roles);
        claims.put(CLAIM_TOKEN_TYPE, tokenType);

        return Jwts.builder()
                .claims(claims)
                .subject(username)
                .issuedAt(now)
                .expiration(expiryDate)
                .issuer(jwtProperties.getIssuer())
                .audience()
                .add(jwtProperties.getAudience())
                .and()
                .signWith(getSigningKey())
                .compact();
    }

    public String getUsernameFromToken(String token) {
        Claims claims = parseToken(token);
        return claims != null ? claims.getSubject() : null;
    }

    public Long getUserIdFromToken(String token) {
        Claims claims = parseToken(token);
        if (claims != null && claims.get(CLAIM_USER_ID) != null) {
            Object userId = claims.get(CLAIM_USER_ID);
            if (userId instanceof Number) {
                return ((Number) userId).longValue();
            }
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    public List<String> getRolesFromToken(String token) {
        Claims claims = parseToken(token);
        if (claims != null && claims.get(CLAIM_ROLES) != null) {
            Object roles = claims.get(CLAIM_ROLES);
            if (roles instanceof List) {
                return (List<String>) roles;
            }
        }
        return List.of();
    }

    public String getTokenType(String token) {
        Claims claims = parseTokenSilently(token);
        if (claims != null && claims.get(CLAIM_TOKEN_TYPE) != null) {
            return claims.get(CLAIM_TOKEN_TYPE).toString();
        }
        return null;
    }

    /**
     * 验证 Access Token（仅接受 type=access 的token）
     */
    public boolean validateAccessToken(String token) {
        try {
            Claims claims = parseToken(token);
            String type = claims.get(CLAIM_TOKEN_TYPE, String.class);
            return TOKEN_TYPE_ACCESS.equals(type);
        } catch (ExpiredJwtException e) {
            String type = e.getClaims() != null ? e.getClaims().get(CLAIM_TOKEN_TYPE, String.class) : null;
            if (TOKEN_TYPE_ACCESS.equals(type)) {
                log.debug("Access Token已过期（可刷新）");
            }
        } catch (Exception e) {
            log.warn("Access Token验证失败: {}", e.getMessage());
        }
        return false;
    }

    /**
     * 验证 Refresh Token（仅接受 type=refresh 的token，未过期即有效）
     */
    public boolean validateRefreshToken(String token) {
        try {
            Claims claims = parseToken(token);
            String type = claims.get(CLAIM_TOKEN_TYPE, String.class);
            return TOKEN_TYPE_REFRESH.equals(type);
        } catch (Exception e) {
            log.warn("Refresh Token验证失败: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 通用验证（兼容旧版无type字段的token，用于过渡期）
     */
    public boolean validateToken(String token) {
        try {
            parseToken(token);
            return true;
        } catch (ExpiredJwtException e) {
            log.debug("JWT Token已过期: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            log.warn("不支持的JWT Token: {}", e.getMessage());
        } catch (MalformedJwtException e) {
            log.warn("JWT Token格式错误: {}", e.getMessage());
        } catch (SecurityException e) {
            log.warn("JWT Token签名验证失败: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.warn("JWT Token为空或非法: {}", e.getMessage());
        }
        return false;
    }

    /**
     * 检查 token 是否为过期但格式正确的 Access Token（可刷新）
     */
    public boolean isExpiredAccessToken(String token) {
        try {
            parseToken(token);
            return false;
        } catch (ExpiredJwtException e) {
            String type = e.getClaims() != null ? e.getClaims().get(CLAIM_TOKEN_TYPE, String.class) : null;
            return TOKEN_TYPE_ACCESS.equals(type);
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isTokenExpired(String token) {
        try {
            Claims claims = parseToken(token);
            return claims.getExpiration().before(new Date());
        } catch (ExpiredJwtException e) {
            return true;
        }
    }

    public Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private Claims parseTokenSilently(String token) {
        try {
            return parseToken(token);
        } catch (ExpiredJwtException e) {
            return e.getClaims();
        } catch (Exception e) {
            return null;
        }
    }

    public long getAccessExpiration() {
        return jwtProperties.getAccessExpiration();
    }

    public long getRefreshExpiration() {
        return jwtProperties.getRefreshExpiration();
    }

    public long getExpirationTime() {
        return jwtProperties.getAccessExpiration();
    }
}