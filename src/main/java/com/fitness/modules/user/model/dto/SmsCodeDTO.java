package com.fitness.modules.user.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * 短信验证码请求DTO
 */
@Data
@Schema(description = "发送短信验证码请求")
public class SmsCodeDTO {

    /**
     * 手机号
     */
    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;

    /**
     * 滑块验证令牌
     */
    @NotBlank(message = "验证令牌不能为空")
    private String verifyToken;
}
