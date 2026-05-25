package com.fitness.modules.knowledge.controller;

//import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fitness.common.result.Result;
import com.fitness.modules.knowledge.model.dto.KnowledgeCategoryDTO;
import com.fitness.modules.knowledge.model.vo.KnowledgeCategoryVO;
import com.fitness.modules.knowledge.service.KnowledgeCategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "知识分类管理", description = "知识库分类的增删改查管理")
@RestController
@RequestMapping("/api/v1/knowledge/categories")
@RequiredArgsConstructor
public class KnowledgeCategoryController {

    private final KnowledgeCategoryService categoryService;

    @Operation(summary = "获取所有知识分类")
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Result<List<KnowledgeCategoryVO>> listAll() {
        return Result.success(categoryService.listAll());
    }

    @Operation(summary = "根据ID获取知识分类")
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<KnowledgeCategoryVO> getById(@PathVariable Long id) {
        return Result.success(categoryService.getById(id));
    }

    @Operation(summary = "创建知识分类")
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Long> create(@Valid @RequestBody KnowledgeCategoryDTO dto) {
        return Result.success(categoryService.create(dto));
    }

    @Operation(summary = "更新知识分类")
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody KnowledgeCategoryDTO dto) {
        dto.setId(id);
        categoryService.update(dto);
        return Result.success();
    }

    @Operation(summary = "删除知识分类")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> delete(@PathVariable Long id) {
        categoryService.delete(id);
        return Result.success();
    }
}
