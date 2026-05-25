package com.fitness.modules.user.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "刷新访问令牌请求")
public class RefreshTokenDTO {

    @NotBlank(message = "Refresh Token不能为空")
    private String refreshToken;
}
