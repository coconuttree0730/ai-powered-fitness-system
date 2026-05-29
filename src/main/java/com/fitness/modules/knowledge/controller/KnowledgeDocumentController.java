package com.fitness.modules.knowledge.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fitness.common.result.Result;
import com.fitness.modules.knowledge.model.dto.KnowledgeDocumentDTO;
import com.fitness.modules.knowledge.model.dto.KnowledgeDocumentQueryDTO;
import com.fitness.modules.knowledge.model.vo.KnowledgeDocumentDetailVO;
import com.fitness.modules.knowledge.model.vo.KnowledgeDocumentVO;
import com.fitness.modules.knowledge.service.KnowledgeDocumentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * 知识文档控制器
 */

@Tag(name = "知识文档管理", description = "知识库文档的增删改查与发布管理")
@RestController
@RequestMapping("/api/v1/knowledge/documents")
@RequiredArgsConstructor
public class KnowledgeDocumentController {

    private final KnowledgeDocumentService documentService;

    /**
     * 分页查询
     */
    @Operation(summary = "分页查询知识文档")
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Page<KnowledgeDocumentVO>> page(KnowledgeDocumentQueryDTO queryDTO) {
        return Result.success(documentService.page(queryDTO));
    }

    /**
     * 获取详情
     */
    @Operation(summary = "获取知识文档详情")
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<KnowledgeDocumentDetailVO> getDetail(@PathVariable Long id) {
        return Result.success(documentService.getDetailById(id));
    }

    /**
     * 创建 知识库文档
     */
    @Operation(summary = "创建知识文档")
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Long> create(@Valid @RequestBody KnowledgeDocumentDTO dto) {
        return Result.success(documentService.create(dto));
    }

    @Operation(summary = "上传知识文档")
    @PostMapping("/upload")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Long> upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "title", required = false) String title,
            @RequestParam(value = "categoryCode", required = false) String categoryCode) {
        return Result.success(documentService.uploadDocument(file, title, categoryCode));
    }

    @Operation(summary = "更新知识文档")
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody KnowledgeDocumentDTO dto) {
        dto.setId(id);
        documentService.update(dto);
        return Result.success();
    }

    @Operation(summary = "删除知识文档")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> delete(@PathVariable Long id) {
        documentService.delete(id);
        return Result.success();
    }

    /**
     * 发布
     */
    @Operation(summary = "发布知识文档")
    @PatchMapping("/{id}/publish")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> publish(@PathVariable Long id) {
        documentService.publish(id);
        return Result.success();
    }

    /**
     * 归档
     */
    @Operation(summary = "归档知识文档")
    @PatchMapping("/{id}/archive")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> archive(@PathVariable Long id) {
        documentService.archive(id);
        return Result.success();
    }

    /**
     * 重建索引
     */
    @Operation(summary = "重建文档索引")
    @PostMapping("/{id}/reindex")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> reindex(@PathVariable Long id) {
        documentService.reindex(id);
        return Result.success();
    }
}
