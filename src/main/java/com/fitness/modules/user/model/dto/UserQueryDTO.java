package com.fitness.modules.user.model.dto;

import lombok.Data;

@Data
public class UserQueryDTO {

    private String username;

    private String phone;

    private String role;

    private Integer status;

    private Integer pageNum = 1;

    private Integer pageSize = 10;
}
