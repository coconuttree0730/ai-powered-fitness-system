package com.fitness.modules.user.model.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RefreshTokenDTO {

    @NotBlank(message = "Refresh Token不能为空")
    private String refreshToken;
}