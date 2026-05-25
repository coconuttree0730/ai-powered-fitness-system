package com.fitness.modules.user.model.dto;

import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * 用户更新请求DTO（用于管理员编辑用户）
 * 不包含密码字段，密码通过单独的重置密码接口处理
 */
@Data
public class UserUpdateDTO {

    /**
     * 手机号
     */
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;

    /**
     * 邮箱
     */
    @Pattern(regexp = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$", message = "邮箱格式不正确")
    private String email;

    /**
     * 头像URL
     */
    private String avatar;
}
