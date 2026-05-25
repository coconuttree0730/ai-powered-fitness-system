package com.fitness.integration.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fitness.common.result.Result;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * JWT 认证过滤器
 * 从请求头中解析JWT Token并设置SecurityContext
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    // JWT Token提供者
    private final JwtTokenProvider jwtTokenProvider;
    // 用户详情服务实现
    private final UserDetailsServiceImpl userDetailsService;
    // JSON工具
    private final ObjectMapper objectMapper;

    /**
     * Authorization请求头名称
     */
    private static final String AUTHORIZATION_HEADER = "Authorization";

    /**
     * Bearer Token前缀
     */
    private static final String BEARER_PREFIX = "Bearer ";

    /**
     * 拦截所有请求，进行JWT认证 前置过滤器！！！
     *
     * @param request     HTTP请求
     * @param response    HTTP响应
     * @param filterChain 过滤器链
     */
    @Override
    protected void doFilterInternal(@NotNull HttpServletRequest request,
                                    @NotNull HttpServletResponse response,
                                    @NotNull FilterChain filterChain) {
        try {
            // 1. 从请求中获取JWT Token
            String jwt = getJwtFromRequest(request);
            
            log.debug("JWT认证过滤器执行, URI: {}, Token存在: {}", request.getRequestURI(), jwt != null);

            // 2. 验证Token : 底层原理： 判断是不是 null
            if (StringUtils.hasText(jwt)) {
                if (jwtTokenProvider.validateAccessToken(jwt)) {
                    // 3. 从Token中获取用户ID
                    Long userId = jwtTokenProvider.getUserIdFromToken(jwt);
                    // 4. 创建用户详情
                    if (userId != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                        // 4. 加载用户详情
                        UserDetails userDetails = userDetailsService.loadUserById(userId);

                        // 5. 创建认证对象
                        UsernamePasswordAuthenticationToken authentication =
                                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities()
                                );
                        // 7. 设置认证对象
                        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                        // 6. 设置SecurityContext 添加权限信息
                        SecurityContextHolder.getContext().setAuthentication(authentication);

                        log.debug("JWT认证成功, 用户ID: {}, URI: {}", userId, request.getRequestURI());
                    }
                } else {
                    log.warn("JWT Token验证失败, URI: {}", request.getRequestURI());
                    // Token无效，返回401
                    sendUnauthorizedResponse(response, "Token无效或已过期");
                    return;
                }
            } else {
                log.debug("请求未携带JWT Token, URI: {}", request.getRequestURI());
            }
        } catch (Exception e) {
            // 检查是否是客户端断开连接
            if (isClientAbortException(e)) {
                log.debug("客户端在认证过程中断开连接: {}", e.getMessage());
                return;
            }
            log.error("JWT认证过程中发生错误: {}", e.getMessage(), e);
            sendUnauthorizedResponse(response, "认证失败: " + e.getMessage());
            return;
        }
        //放行
        try {
            filterChain.doFilter(request, response);
        } catch (Exception e) {
            // 检查是否是客户端断开连接
            if (isClientAbortException(e)) {
                log.debug("客户端在请求处理过程中断开连接: {}", e.getMessage());
            } else {
                log.error("过滤器链执行失败: {}", e.getMessage(), e);
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * 从请求中获取JWT Token
     *
     * @param request HTTP请求
     * @return JWT Token字符串，如果不存在则返回null
     */
    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader(AUTHORIZATION_HEADER);
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(BEARER_PREFIX)) {
            return bearerToken.substring(BEARER_PREFIX.length());
        }
        return null;
    }

    /**
     * 判断异常是否是客户端断开连接异常
     *
     * @param throwable 异常
     * @return true-客户端断开连接, false-其他异常
     */
    private boolean isClientAbortException(Throwable throwable) {
        String className = throwable.getClass().getName();
        if (className.contains("ClientAbortException")) {
            return true;
        }
        Throwable cause = throwable.getCause();
        while (cause != null) {
            if (cause.getClass().getName().contains("ClientAbortException")) {
                return true;
            }
            cause = cause.getCause();
        }
        return false;
    }

    /**
     * 发送401未授权响应
     *
     * @param response HTTP响应
     * @param message  错误消息
     */
    private void sendUnauthorizedResponse(HttpServletResponse response, String message) {
        try {
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setCharacterEncoding("UTF-8");

            Result<Void> result = Result.error(401, message);
            objectMapper.writeValue(response.getOutputStream(), result);
        } catch (IOException e) {
            if (isClientAbortException(e)) {
                log.debug("客户端在响应完成前断开连接: {}", e.getMessage());
            } else {
                log.warn("发送认证失败响应时发生IO异常: {}", e.getMessage());
            }
        }
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        // 白名单URL不需要过滤（与 SecurityConfig 保持一致）
        String path = request.getRequestURI();
        
        // 精确匹配的公开路径
        List<String> exactMatchPaths = List.of(
                "/api/v1/auth/login",
                "/api/v1/auth/login/sms",
                "/api/v1/auth/register",
                "/api/v1/auth/refresh",
                "/api/v1/auth/slider-verify",
                "/api/v1/auth/sms-code",
                "/api/v1/banners/active",
                "/api/v1/announcements/published",
                "/api/v1/coaches/home",
                "/api/v1/payment/alipay/notify",
                "/swagger-ui.html",
                "/error",
                "/favicon.ico"
        );
        
        // 前缀匹配的公开路径
        List<String> prefixMatchPaths = List.of(
                "/api/v1/auth/slider-verify/",
                "/api/v1/courses/public/",
                "/api/v1/coaches/home",
                "/api/v1/equipment/",
                "/static/",
                "/uploads/",
                "/swagger-ui/",
                "/v3/api-docs/",
                "/swagger-resources/",
                "/webjars/"
        );
        
        // 检查精确匹配
        if (exactMatchPaths.contains(path)) {
            log.debug("shouldNotFilter检查: path={}, 精确匹配白名单", path);
            return true;
        }
        
        // 检查前缀匹配
        boolean shouldSkip = prefixMatchPaths.stream().anyMatch(path::startsWith);
        log.debug("shouldNotFilter检查: path={}, shouldSkip={}", path, shouldSkip);
        return shouldSkip;
    }
}
