package com.fitness.common.exception;

import com.fitness.common.constants.ErrorCode;
import com.fitness.common.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 用于处理业务异常，返回业务异常的错误码和错误信息
     * @param e 业务异常
     * @return 业务异常的错误码和错误信息
     */
    @ExceptionHandler(BusinessException.class)
    public Result<Void> handleBusinessException(BusinessException e) {
        log.warn("业务异常: {}", e.getMessage());
        return Result.error(e.getCode(), e.getMessage());
    }

    /**
     * 用于处理方法__参数校验__失败的情况，返回参数校验错误信息
     * @param e 参数校验失败异常
     * @return 参数校验错误信息
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<Map<String, String>> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        Map<String, String> errors = new HashMap<>();
        e.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        log.warn("参数校验失败: {}", errors);
        return Result.error(ErrorCode.PARAM_ERROR.getCode(), "参数校验失败: " + errors);
    }

    /**
     * 用于处理参数绑定失败的情况，返回参数绑定错误信息
     * @param e 参数绑定失败异常
     * @return 参数绑定错误信息
     */
    @ExceptionHandler(BindException.class)
    public Result<Map<String, String>> handleBindException(BindException e) {
        Map<String, String> errors = new HashMap<>();
        e.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        log.warn("参数绑定失败: {}", errors);
        return Result.error(ErrorCode.PARAM_ERROR.getCode(), "参数绑定失败: " + errors);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public Result<Void> handleAccessDeniedException(AccessDeniedException e) {
        log.warn("权限不足: {}", e.getMessage());
        return Result.error(ErrorCode.FORBIDDEN);
    }

    /**
     * 处理 Spring Security 6+ 的 AuthorizationDeniedException
     * 用于 @PreAuthorize 等注解的权限验证失败
     */
    @ExceptionHandler(AuthorizationDeniedException.class)
    public Result<Void> handleAuthorizationDeniedException(AuthorizationDeniedException e) {
        log.warn("授权被拒绝: {}", e.getMessage());
        return Result.error(ErrorCode.FORBIDDEN);
    }

    @ExceptionHandler(Exception.class)
    public Result<Void> handleException(Exception e) {
        log.error("系统异常: ", e);
        return Result.error(ErrorCode.INTERNAL_ERROR);
    }
}
