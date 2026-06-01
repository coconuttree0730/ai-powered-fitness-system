package com.fitness.modules.knowledge.controller;

import com.fitness.common.result.Result;
import com.fitness.modules.knowledge.model.dto.RAGDebugQueryDTO;
import com.fitness.modules.knowledge.model.vo.RAGDebugResultVO;
import com.fitness.modules.knowledge.service.RAGService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "AI知识检索管理", description = "RAG调试与评测管理接口")
@RestController
@RequestMapping("/api/v1/admin/knowledge/rag")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class RAGAdminController {

    private final RAGService ragService;

    @Operation(summary = "RAG调试检索")
    @PostMapping("/debug")
    public Result<RAGDebugResultVO> debugSearch(@Valid @RequestBody RAGDebugQueryDTO queryDTO) {
        return Result.success(ragService.debugSearch(queryDTO));
    }
}
