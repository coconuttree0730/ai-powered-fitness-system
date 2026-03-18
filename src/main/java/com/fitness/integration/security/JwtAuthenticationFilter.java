package com.fitness.integration.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fitness.common.result.Result;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
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

    private final JwtTokenProvider jwtTokenProvider;
    private final UserDetailsServiceImpl userDetailsService;
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
     * 拦截所有请求，进行JWT认证
     *
     * @param request     HTTP请求
     * @param response    HTTP响应
     * @param filterChain 过滤器链
     * @throws ServletException Servlet异常
     * @throws IOException      IO异常
     */
    @Override
    protected void doFilterInternal(@NotNull HttpServletRequest request,
                                    @NotNull HttpServletResponse response,
                                    @NotNull FilterChain filterChain) throws ServletException, IOException {
        try {
            // 1. 从请求中获取JWT Token
            String jwt = getJwtFromRequest(request);

            // 2. 验证Token : 底层原理： 判断是不是 null
            if (StringUtils.hasText(jwt)) {
                if (jwtTokenProvider.validateToken(jwt)) {
                    // 3. 从Token中获取用户ID
                    Long userId = jwtTokenProvider.getUserIdFromToken(jwt);
                    // 4. 创建用户详情
                    if (userId != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                        // 4. 加载用户详情
                        UserDetails userDetails = userDetailsService.loadUserById(userId);

                        // 5. 创建认证对象
                        UsernamePasswordAuthenticationToken authentication =
                                new UsernamePasswordAuthenticationToken(
                                        userDetails,
                                        null,
                                        userDetails.getAuthorities()
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
            }
        } catch (Exception e) {
            log.error("JWT认证过程中发生错误: {}", e.getMessage(), e);
            sendUnauthorizedResponse(response, "认证失败: " + e.getMessage());
            return;
        }
        //放行
        filterChain.doFilter(request, response);
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
     * 发送401未授权响应
     *
     * @param response HTTP响应
     * @param message  错误消息
     * @throws IOException IO异常
     */
    private void sendUnauthorizedResponse(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        Result<Void> result = Result.error(401, message);
        objectMapper.writeValue(response.getOutputStream(), result);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        // 白名单URL不需要过滤
        String path = request.getRequestURI();
        List<String> whiteList = List.of(
                "/api/v1/auth/login",
                "/api/v1/auth/register",
                "/api/v1/courses/public/",
                "/swagger-ui/",
                "/v3/api-docs/",
                "/swagger-ui.html",
                "/webjars/",
                "/error"
        );

        return whiteList.stream().anyMatch(path::startsWith);
    }
}
