package com.fitness.integration.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fitness.common.result.Result;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * JWT 认证入口点
 * 处理 未认证 (401 )用户的请求
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException {
        // 如果响应已提交（如SSE流已关闭），则不再处理
        if (response.isCommitted()) {
            log.debug("响应已提交，跳过认证入口点处理: {}", request.getRequestURI());
            return;
        }

        log.warn("未认证访问: {}, URI: {}", authException.getMessage(), request.getRequestURI());

        response.setStatus(HttpStatus.UNAUTHORIZED.value()); // 401
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        Result<Void> result = Result.error(401, "未登录或Token已过期，请先登录");
        objectMapper.writeValue(response.getOutputStream(), result);
    }
}
