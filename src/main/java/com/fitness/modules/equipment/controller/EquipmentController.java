package com.fitness.modules.equipment.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fitness.common.result.Result;
import com.fitness.modules.equipment.model.dto.EquipmentQueryDTO;
import com.fitness.modules.equipment.model.vo.EquipmentVO;
import com.fitness.modules.equipment.service.EquipmentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

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
    public Result<Page<EquipmentVO>> getEquipmentList(EquipmentQueryDTO query) {
        log.info("获取器材列表请求: keyword={}, status={}", query.getKeyword(), query.getStatus());
        Page<EquipmentVO> page = equipmentService.getEquipmentList(query);
        return Result.success(page);
    }
}
