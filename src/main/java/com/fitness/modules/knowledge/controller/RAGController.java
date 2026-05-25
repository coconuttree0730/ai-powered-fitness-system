package com.fitness.modules.knowledge.controller;

import com.fitness.common.result.Result;
import com.fitness.modules.knowledge.model.dto.RAGQueryDTO;
import com.fitness.modules.knowledge.model.vo.RAGSearchResultVO;
import com.fitness.modules.knowledge.service.RAGService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Tag(name = "AI知识检索", description = "RAG向量检索与AI对话接口")
@RestController
@RequestMapping("/api/v1/knowledge/rag")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class RAGController {

    private final RAGService ragService;

    /**
     * RAG搜索接口 向量匹配
     *
     * @param queryDTO 查询参数
     * @return 搜索结果
     */
    @Operation(summary = "向量匹配搜索")
    @PostMapping("/search")
    public Result<RAGSearchResultVO> search(@Valid @RequestBody RAGQueryDTO queryDTO) {
        return Result.success(ragService.search(queryDTO));
    }

    /**
     * RAG搜索接口 混合匹配
     * @param query 查询参数
     * @param categoryId 分类ID
     * @return 搜索结果
     */
    @Operation(summary = "RAG混合检索对话")
    @PostMapping("/chat")
    public Result<String> chat(
            @RequestParam("query") String query,
            @RequestParam(value = "categoryId", required = false) Long categoryId) {
        return Result.success(ragService.chatWithRAG(query, categoryId));
    }
}
