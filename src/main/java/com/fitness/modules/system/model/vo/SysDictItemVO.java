package com.fitness.modules.system.model.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

@Data
public class SysDictItemVO {

    private Long id;
    private Long dictId;
    private String label;
    private String value;
    private String description;
    private String status;
    private Integer sortOrder;
    private Map<String, Object> extra;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

/**
 * 字典选项（用于下拉选择）
 */
@Data
class DictOptionVO {
    private String label;
    private String value;
}
