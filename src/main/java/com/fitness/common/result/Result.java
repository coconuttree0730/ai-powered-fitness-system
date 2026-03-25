package com.fitness.common.result;

import com.fitness.common.constants.ErrorCode;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 结果封装类
 *
 * @param <T> 数据类型
 * 例：
 * Result<User> result = Result.success(user);
 * Result<User> result = Result.success();
 * Result<User> result = Result.error("错误信息");
 * Result<User> result = Result.error(ErrorCode.INTERNAL_ERROR);
 * Result<User> result = Result.error(400, "参数错误");
 * Result<User> result = Result.error(404, "资源不存在");
 * Result<User> result = Result.error(500, "服务器错误");
 * Result<User> result = Result.error(500, "服务器错误", null);
 */
@Data
public class Result<T> {

    private Integer code;
    private String message;
    private T data;
    private LocalDateTime timestamp;

    private Result() {
        this.timestamp = LocalDateTime.now();
    }

    private Result(Integer code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
        this.timestamp = LocalDateTime.now();
    }

    public static <T> Result<T> success(T data) {
        return new Result<>(ErrorCode.SUCCESS.getCode(), ErrorCode.SUCCESS.getMessage(), data);
    }

    public static <T> Result<T> success() {
        return new Result<>(ErrorCode.SUCCESS.getCode(), ErrorCode.SUCCESS.getMessage(), null);
    }

    public static <T> Result<T> error(String message) {
        return new Result<>(ErrorCode.INTERNAL_ERROR.getCode(), message, null);
    }

    public static <T> Result<T> error(Integer code, String message) {
        return new Result<>(code, message, null);
    }

    public static <T> Result<T> error(ErrorCode errorCode) {
        return new Result<>(errorCode.getCode(), errorCode.getMessage(), null);
    }
}
