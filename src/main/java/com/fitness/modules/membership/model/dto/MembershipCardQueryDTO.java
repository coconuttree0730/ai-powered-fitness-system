package com.fitness.modules.membership.model.dto;

import lombok.Data;

@Data
public class MembershipCardQueryDTO {

    private String name;
    private String typeCode;
    private String status;
    private Long page = 1L;
    private Long pageSize = 10L;
}
