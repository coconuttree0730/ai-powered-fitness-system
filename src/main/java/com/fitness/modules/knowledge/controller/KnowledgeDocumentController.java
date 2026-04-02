package com.fitness.modules.knowledge.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fitness.common.result.Result;
import com.fitness.modules.knowledge.model.dto.KnowledgeDocumentDTO;
import com.fitness.modules.knowledge.model.dto.KnowledgeDocumentQueryDTO;
import com.fitness.modules.knowledge.model.vo.KnowledgeDocumentDetailVO;
import com.fitness.modules.knowledge.model.vo.KnowledgeDocumentVO;
import com.fitness.modules.knowledge.service.KnowledgeDocumentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/knowledge/documents")
@RequiredArgsConstructor
public class KnowledgeDocumentController {

    private final KnowledgeDocumentService documentService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Page<KnowledgeDocumentVO>> page(KnowledgeDocumentQueryDTO queryDTO) {
        return Result.success(documentService.page(queryDTO));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<KnowledgeDocumentDetailVO> getDetail(@PathVariable Long id) {
        return Result.success(documentService.getDetailById(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Long> create(@Valid @RequestBody KnowledgeDocumentDTO dto) {
        return Result.success(documentService.create(dto));
    }

    @PostMapping("/upload")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Long> upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "title", required = false) String title) {
        return Result.success(documentService.uploadDocument(file, title));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody KnowledgeDocumentDTO dto) {
        dto.setId(id);
        documentService.update(dto);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> delete(@PathVariable Long id) {
        documentService.delete(id);
        return Result.success();
    }

    @PatchMapping("/{id}/publish")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> publish(@PathVariable Long id) {
        documentService.publish(id);
        return Result.success();
    }

    @PatchMapping("/{id}/archive")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> archive(@PathVariable Long id) {
        documentService.archive(id);
        return Result.success();
    }

    @PostMapping("/{id}/reindex")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> reindex(@PathVariable Long id) {
        documentService.reindex(id);
        return Result.success();
    }
}
