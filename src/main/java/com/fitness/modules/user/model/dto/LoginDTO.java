package com.fitness.modules.user.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 用户登录请求DTO
 * 支持手机号或用户名登录
 */
@Data
@Schema(description = "用户名密码登录请求")
public class LoginDTO {

    @NotBlank(message = "手机号/用户名不能为空")
    private String username;

    @NotBlank(message = "密码不能为空")
    private String password;

    private Boolean rememberMe = false;
}
