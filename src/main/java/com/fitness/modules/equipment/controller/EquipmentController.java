package com.fitness.modules.equipment.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fitness.common.result.PageResult;
import com.fitness.common.result.Result;
import com.fitness.modules.equipment.model.dto.EquipmentQueryDTO;
import com.fitness.modules.equipment.model.vo.EquipmentVO;
import com.fitness.modules.equipment.model.vo.RepairVO;
import com.fitness.modules.equipment.service.EquipmentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 器材查询控制器（公开接口）
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/equipment")
@RequiredArgsConstructor
public class EquipmentController {

    private final EquipmentService equipmentService;

    /**
     * 获取器材详情
     *
     * @param id 器材ID
     * @return 器材详情
     */
    @GetMapping("/{id}")
    public Result<EquipmentVO> getEquipmentById(@PathVariable Long id) {
        log.info("获取器材详情请求: equipmentId={}", id);
        EquipmentVO equipmentVO = equipmentService.getEquipmentById(id);
        return Result.success(equipmentVO);
    }

    /**
     * 获取器材列表
     *
     * @param query 查询条件
     * @return 器材列表
     */
    @GetMapping("/list")
    public Result<PageResult<EquipmentVO>> getEquipmentList(EquipmentQueryDTO query) {
        log.info("获取器材列表请求: keyword={}, status={}", query.getKeyword(), query.getStatus());
        Page<EquipmentVO> page = equipmentService.getEquipmentList(query);
        return Result.success(PageResult.of(page.getRecords(), page.getTotal()));
    }

    /**
     * 获取器材的报修记录
     *
     * @param id 器材 ID
     * @return 报修记录列表
     */
    @GetMapping("/{id:\\d+}/repairs")
    public Result<List<RepairVO>> getEquipmentRepairs(@PathVariable Long id) {
        log.info("获取器材报修记录请求：equipmentId={}", id);
        List<RepairVO> repairs = equipmentService.getRepairList(id);
        return Result.success(repairs);
    }

    /**
     * 获取首页展示的器材数据
     * 按类型分组展示，用于官网首页器械区域
     *
     * @return 按类型分组的器材列表
     */
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

    /**
     * 获取所有正常状态的器材列表（用于报修选择器械）
     *
     * @return 器材列表
     */
    @GetMapping("/active-list")
    public Result<List<EquipmentVO>> getActiveEquipmentList() {
        log.info("获取正常状态器材列表请求");
        List<EquipmentVO> list = equipmentService.getActiveEquipmentList();
        return Result.success(list);
    }
}
