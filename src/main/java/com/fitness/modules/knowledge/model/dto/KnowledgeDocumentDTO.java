package com.fitness.modules.knowledge.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class KnowledgeDocumentDTO {
    private Long id;

    @NotBlank(message = "文档标题不能为空")
    @Size(max = 200, message = "文档标题最长200个字符")
    private String title;

    private Integer status;

    private Long categoryId;

    private MultipartFile file;
}
