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

import java.util.List;

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
}
