package com.fitness.modules.user.model.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 用户登录请求DTO
 * 支持手机号或用户名登录
 */
@Data
public class LoginDTO {

    /**
     * 登录账号（手机号或用户名）
     */
    @NotBlank(message = "手机号/用户名不能为空")
    private String username;

    /**
     * 密码
     */
    @NotBlank(message = "密码不能为空")
    private String password;
}
