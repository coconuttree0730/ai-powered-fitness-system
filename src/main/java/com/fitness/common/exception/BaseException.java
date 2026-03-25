package com.fitness.common.exception;

import lombok.Getter;

/**
 * 项目基础异常类
 * 所有自定义异常都应继承此类
 */
@Getter
public class BaseException extends RuntimeException {

    private final Integer code;
    private final String message;

    public BaseException(Integer code, String message) {
        super(message);
        this.code = code;
        this.message = message;
    }

    public BaseException(Integer code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
        this.message = message;
    }
}
