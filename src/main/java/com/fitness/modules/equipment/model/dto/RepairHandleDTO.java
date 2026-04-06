package com.fitness.modules.equipment.model.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 报修处理请求DTO
 */
@Data
public class RepairHandleDTO {

    /**
     * 状态：0-待处理, 1-处理中, 2-已完成, 3-已关闭
     */
    @NotNull(message = "状态不能为空")
    private Integer status;

    /**
     * 处理备注
     */
    private String remark;
}
