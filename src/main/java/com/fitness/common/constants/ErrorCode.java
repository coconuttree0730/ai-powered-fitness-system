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
    SMS_CODE_ERROR(1004, "短信验证码错误"),
    SMS_CODE_EXPIRED(1005, "短信验证码已过期"),
    SMS_CODE_SEND_TOO_FREQUENT(1006, "短信验证码发送过于频繁"),
    PHONE_NOT_BOUND(1007, "手机号未绑定用户"),
    USERNAME_ALREADY_EXISTS(1008, "用户名已存在"),
    PHONE_ALREADY_BOUND(1009, "手机号已被其他用户绑定"),
    EMAIL_ALREADY_BOUND(1010, "邮箱已被其他用户绑定"),
    EMAIL_CODE_ERROR(1011, "邮箱验证码错误"),
    OLD_PHONE_VERIFY_REQUIRED(1012, "请先验证原手机号"),
    SMS_CODE_DAILY_LIMIT_EXCEEDED(1013, "今日短信验证码发送次数已达上限，请明天再试"),

    COURSE_NOT_FOUND(2001, "课程不存在"),
    COURSE_FULL(2002, "课程已满"),

    BOOKING_NOT_FOUND(2101, "预约记录不存在"),
    BOOKING_ALREADY_EXISTS(2102, "已预约该课程"),
    BOOKING_CANNOT_CANCEL(2103, "预约无法取消"),
    BOOKING_STATUS_ERROR(2104, "预约状态错误"),

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

    ANALYSIS_ERROR(6001, "数据分析失败"),

    PRODUCT_NOT_FOUND(7001, "商品不存在"),
    PRODUCT_STOCK_INSUFFICIENT(7002, "商品库存不足"),
    POINTS_INSUFFICIENT(7003, "积分不足"),
    ORDER_NOT_FOUND(7004, "订单不存在"),
    ORDER_STATUS_ERROR(7005, "订单状态错误"),

    DATA_NOT_FOUND(8001, "数据不存在"),
    DATA_ALREADY_EXISTS(8002, "数据已存在");

    private final Integer code;
    private final String message;

    ErrorCode(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

}
