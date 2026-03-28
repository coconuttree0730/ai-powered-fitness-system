package com.fitness.modules.user.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * 更新手机号DTO
 */
@Data
public class UpdatePhoneDTO {

    /**
     * 新手机号
     */
    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;

    /**
     * 新手机号验证码
     */
    @NotBlank(message = "验证码不能为空")
    private String code;

    /**
     * 旧手机号验证码（用于验证身份）
     */
    @NotBlank(message = "原手机号验证码不能为空")
    private String oldCode;
}
