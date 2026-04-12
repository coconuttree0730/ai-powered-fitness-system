package com.fitness.modules.knowledge.model.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class KnowledgeDocumentVO {
    private Long id;

    private String title;

    private String fileUrl;

    private String fileName;

    private String fileType;

    private Long fileSize;

    private Integer status;

    private String statusDesc;

    private Integer chunkCount;

    private Long categoryId;

    private String categoryName;

    private Long createBy;

    private String createByName;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
