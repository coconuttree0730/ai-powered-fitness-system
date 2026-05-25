package com.fitness.modules.equipment.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fitness.common.result.PageResult;
import com.fitness.common.result.Result;
import com.fitness.modules.equipment.model.dto.EquipmentQueryDTO;
import com.fitness.modules.equipment.model.vo.EquipmentVO;
import com.fitness.modules.equipment.model.vo.RepairVO;
import com.fitness.modules.equipment.service.EquipmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/equipment")
@RequiredArgsConstructor
@Tag(name = "器材浏览", description = "器材查询公开接口")
public class EquipmentController {

    private final EquipmentService equipmentService;

    @Operation(summary = "获取器材详情")
    @GetMapping("/{id}")
    public Result<EquipmentVO> getEquipmentById(@PathVariable Long id) {
        log.info("获取器材详情请求: equipmentId={}", id);
        EquipmentVO equipmentVO = equipmentService.getEquipmentById(id);
        return Result.success(equipmentVO);
    }

    @Operation(summary = "获取器材列表")
    @GetMapping("/list")
    public Result<PageResult<EquipmentVO>> getEquipmentList(@Valid EquipmentQueryDTO query) {
        log.info("获取器材列表请求: keyword={}, status={}", query.getKeyword(), query.getStatus());
        Page<EquipmentVO> page = equipmentService.getEquipmentList(query);
        return Result.success(PageResult.of(page.getRecords(), page.getTotal()));
    }

    @Operation(summary = "获取器材的报修记录")
    @GetMapping("/{id:\\d+}/repairs")
    public Result<List<RepairVO>> getEquipmentRepairs(@PathVariable Long id) {
        log.info("获取器材报修记录请求：equipmentId={}", id);
        List<RepairVO> repairs = equipmentService.getRepairList(id);
        return Result.success(repairs);
    }

    @Operation(summary = "获取首页展示的器材数据")
    @GetMapping("/homepage")
    public Result<Map<String, Object>> getHomePageEquipments() {
        log.info("获取首页器材展示数据请求");

        // 查询各类型器材（限制数量）
        EquipmentQueryDTO query = new EquipmentQueryDTO();
        query.setPageNum(1);
        query.setPageSize(100);
        query.setStatus(1); // 只查询正常状态的器材

        Page<EquipmentVO> page = equipmentService.getEquipmentList(query);
        List<EquipmentVO> allEquipments = page.getRecords();

        // 按类型分组
        Map<String, List<EquipmentVO>> groupedByType = new HashMap<>();
        for (EquipmentVO equipment : allEquipments) {
            String typeCode = equipment.getTypeCode();
            if (typeCode == null) {
                typeCode = "OTHER";
            }
            groupedByType.computeIfAbsent(typeCode, k -> new java.util.ArrayList<>()).add(equipment);
        }

        // 构建返回结果
        Map<String, Object> result = new HashMap<>();
        result.put("equipments", allEquipments);
        result.put("groupedByType", groupedByType);
        result.put("total", allEquipments.size());

        return Result.success(result);
    }

    @Operation(summary = "获取所有正常状态的器材列表")
    @GetMapping("/active-list")
    public Result<List<EquipmentVO>> getActiveEquipmentList() {
        log.info("获取正常状态器材列表请求");
        List<EquipmentVO> list = equipmentService.getActiveEquipmentList();
        return Result.success(list);
    }
}
