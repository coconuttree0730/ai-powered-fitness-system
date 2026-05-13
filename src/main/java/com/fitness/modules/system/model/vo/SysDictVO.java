package com.fitness.modules.system.model.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class SysDictVO {

    private Long id;
    private String dictName;
    private String dictCode;
    private String description;
    private String status;
    private Integer sortOrder;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<SysDictItemVO> items;
}
