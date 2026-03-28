package com.fitness.modules.user.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 更新用户名DTO
 */
@Data
public class UpdateUsernameDTO {

    /**
     * 新用户名
     */
    @NotBlank(message = "用户名不能为空")
    @Size(min = 2, max = 20, message = "用户名长度应为2-20位")
    private String username;
}
