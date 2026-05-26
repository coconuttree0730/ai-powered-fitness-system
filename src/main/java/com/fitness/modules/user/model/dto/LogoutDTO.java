package com.fitness.modules.user.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "登出请求")
public class LogoutDTO {

    @Schema(description = "Refresh Token，用于同时注销刷新令牌")
    private String refreshToken;
}
