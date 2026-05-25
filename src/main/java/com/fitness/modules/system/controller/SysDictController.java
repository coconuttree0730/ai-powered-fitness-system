package com.fitness.modules.system.controller;

import com.fitness.common.result.Result;
import com.fitness.modules.system.model.dto.SysDictDTO;
import com.fitness.modules.system.service.SysDictService;
import com.fitness.modules.system.model.vo.SysDictVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "数据字典管理")
@RestController
@RequestMapping("/api/v1/admin/dict")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class SysDictController {

    private final SysDictService dictService;

    @Operation(summary = "获取字典列表")
    @GetMapping("/list")
    public Result<List<SysDictVO>> listAll() {
        return Result.success(dictService.listAllDicts());
    }

    @Operation(summary = "获取字典详情")
    @GetMapping("/{id}")
    public Result<SysDictVO> getById(@PathVariable Long id) {
        return Result.success(dictService.getDictById(id));
    }

    @Operation(summary = "根据编码获取字典")
    @GetMapping("/code/{dictCode}")
    public Result<SysDictVO> getByCode(@PathVariable String dictCode) {
        return Result.success(dictService.getDictByCode(dictCode));
    }

    @Operation(summary = "创建字典")
    @PostMapping
    public Result<SysDictVO> create(@RequestBody SysDictDTO dto) {
        return Result.success(dictService.createDict(dto));
    }

    @Operation(summary = "更新字典")
    @PutMapping("/{id}")
    public Result<SysDictVO> update(@PathVariable Long id, @RequestBody SysDictDTO dto) {
        return Result.success(dictService.updateDict(id, dto));
    }

    @Operation(summary = "删除字典")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        dictService.deleteDict(id);
        return Result.success();
    }

    @Operation(summary = "获取字典选项（下拉用）")
    @GetMapping("/options/{dictCode}")
    @PreAuthorize("isAuthenticated()")
    public Result<List<Map<String, Object>>> getOptions(@PathVariable String dictCode) {
        return Result.success(dictService.getOptionsByCode(dictCode));
    }
}
