package com.fitness.modules.user.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 通过短信验证码修改密码DTO
 */
@Data
public class UpdatePasswordBySmsDTO {

    /**
     * 短信验证码
     */
    @NotBlank(message = "验证码不能为空")
    private String smsCode;

    /**
     * 新密码
     */
    @NotBlank(message = "新密码不能为空")
    @Size(min = 6, max = 20, message = "密码长度应为6-20位")
    private String newPassword;
}
