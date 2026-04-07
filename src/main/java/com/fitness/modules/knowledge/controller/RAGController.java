package com.fitness.modules.knowledge.controller;

import com.fitness.common.result.Result;
import com.fitness.modules.knowledge.model.dto.RAGQueryDTO;
import com.fitness.modules.knowledge.model.vo.RAGSearchResultVO;
import com.fitness.modules.knowledge.service.RAGService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/knowledge/rag")
@RequiredArgsConstructor
public class RAGController {

    private final RAGService ragService;

    /**
     * RAG搜索接口 向量匹配
     *
     * @param queryDTO 查询参数
     * @return 搜索结果
     */
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
    @PostMapping("/chat")
    public Result<String> chat(
            @RequestParam("query") String query,
            @RequestParam(value = "categoryId", required = false) Long categoryId) {
        return Result.success(ragService.chatWithRAG(query, categoryId));
    }
}
