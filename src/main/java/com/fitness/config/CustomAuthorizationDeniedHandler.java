package com.fitness.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fitness.common.result.Result;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 授权拒绝处理器
 * 专门处理 Spring Security 6+ 的 AuthorizationDeniedException
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CustomAuthorizationDeniedHandler implements AccessDeniedHandler {

    private final ObjectMapper objectMapper;

    @Override
    public void handle(HttpServletRequest request,
                       HttpServletResponse response,
                       org.springframework.security.access.AccessDeniedException accessDeniedException) throws IOException {
        handleException(request, response, accessDeniedException);
    }

    /**
     * 处理 AuthorizationDeniedException
     */
    public void handleAuthorizationDenied(HttpServletRequest request,
                                          HttpServletResponse response,
                                          AuthorizationDeniedException exception) throws IOException {
        handleException(request, response, exception);
    }

    private void handleException(HttpServletRequest request,
                                 HttpServletResponse response,
                                 Exception exception) throws IOException {
        if (response.isCommitted()) {
            log.debug("响应已提交，跳过处理: {}", request.getRequestURI());
            return;
        }

        log.warn("访问被拒绝: {}, URI: {}", exception.getMessage(), request.getRequestURI());

        response.setStatus(HttpStatus.FORBIDDEN.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        Result<Void> result = Result.error(403, "没有权限访问该资源");
        objectMapper.writeValue(response.getOutputStream(), result);
    }
}
