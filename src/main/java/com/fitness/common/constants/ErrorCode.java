package com.fitness.common.constants;

import lombok.Getter;

@Getter
public enum ErrorCode {

    SUCCESS(200, "操作成功"),
    PARAM_ERROR(400, "参数错误"),
    UNAUTHORIZED(401, "未登录或Token已过期"),
    FORBIDDEN(403, "没有权限访问"),
    NOT_FOUND(404, "资源不存在"),
    INTERNAL_ERROR(500, "系统内部错误"),

    USER_NOT_FOUND(1001, "用户不存在"),
    USER_ALREADY_EXISTS(1002, "用户已存在"),
    PASSWORD_ERROR(1003, "密码错误"),

    COURSE_NOT_FOUND(2001, "课程不存在"),
    COURSE_FULL(2002, "课程已满"),

    EQUIPMENT_NOT_FOUND(3001, "器材不存在"),
    EQUIPMENT_IN_USE(3002, "器材正在使用中，无法删除"),
    REPAIR_NOT_FOUND(3003, "报修记录不存在"),
    CANNOT_CANCEL_REPAIR(3004, "报修已处理，无法取消"),

    PLAN_NOT_FOUND(4001, "健身计划不存在"),
    PROFILE_NOT_COMPLETE(4002, "请先完善个人信息"),
    AI_GENERATE_ERROR(4003, "AI生成计划失败"),

    FILE_UPLOAD_ERROR(5001, "文件上传失败"),
    FILE_TYPE_NOT_ALLOWED(5002, "文件类型不允许"),
    FILE_TOO_LARGE(5003, "文件大小超过限制"),
    FILE_NOT_FOUND(5004, "文件不存在"),

    ANALYSIS_ERROR(6001, "数据分析失败");

    private final Integer code;
    private final String message;

    ErrorCode(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

    public Integer getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
