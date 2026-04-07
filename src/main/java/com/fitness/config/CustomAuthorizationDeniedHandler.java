package com.fitness.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fitness.common.result.Result;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * 授权拒绝处理器
 * 实现 AccessDeniedHandler 接口处理 Spring Security 的授权拒绝异常
 * 适用于同步和异步（流式）请求
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CustomAuthorizationDeniedHandler implements AccessDeniedHandler {

    private final ObjectMapper objectMapper;

    @Override
    public void handle(HttpServletRequest request,
                       HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException {
        if (response.isCommitted()) {
            log.warn("响应已提交，无法处理授权拒绝异常: {}", request.getRequestURI());
            return;
        }

        log.warn("访问被拒绝: {}, URI: {}, 用户: {}",
                accessDeniedException.getMessage(),
                request.getRequestURI(),
                request.getUserPrincipal() != null ? request.getUserPrincipal().getName() : "匿名用户");

        response.setStatus(HttpStatus.FORBIDDEN.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        Result<Void> result = Result.error(403, "没有权限访问该资源");
        objectMapper.writeValue(response.getOutputStream(), result);
    }
}
