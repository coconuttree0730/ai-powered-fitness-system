package com.fitness.common.exception;

import com.fitness.common.constants.ErrorCode;
import lombok.Getter;
import lombok.EqualsAndHashCode;

/**
 * 业务异常类
 * 用于业务逻辑错误，如：参数校验失败、业务规则冲突等
 */
@Getter
@EqualsAndHashCode(callSuper = true) //确保 equals/hashCode 包含父类字段（可有可无）
public class BusinessException extends BaseException {

    public BusinessException(String message) {
        super(ErrorCode.INTERNAL_ERROR.getCode(), message);
    }

    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getCode(), errorCode.getMessage());
    }

    public BusinessException(Integer code, String message) {
        super(code, message);
    }
}
