package com.fitness.modules.equipment.controller;

import com.fitness.common.result.Result;
import com.fitness.modules.equipment.model.dto.EquipmentDTO;
import com.fitness.modules.equipment.model.vo.RepairVO;
import com.fitness.modules.equipment.service.EquipmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 器材管理控制器（管理员）
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/admin/equipment")
@RequiredArgsConstructor
public class EquipmentAdminController {

    private final EquipmentService equipmentService;

    /**
     * 创建器材
     *
     * @param dto 器材信息
     * @return 创建的器材ID
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Long> createEquipment(@Valid @RequestBody EquipmentDTO dto) {
        log.info("创建器材请求: equipmentName={}", dto.getEquipmentName());
        Long equipmentId = equipmentService.createEquipment(dto);
        return Result.success(equipmentId);
    }

    /**
     * 更新器材
     *
     * @param id  器材ID
     * @param dto 器材信息
     * @return 操作结果
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> updateEquipment(@PathVariable Long id, @Valid @RequestBody EquipmentDTO dto) {
        log.info("更新器材请求: equipmentId={}", id);
        equipmentService.updateEquipment(id, dto);
        return Result.success();
    }

    /**
     * 删除器材
     *
     * @param id 器材ID
     * @return 操作结果
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> deleteEquipment(@PathVariable Long id) {
        log.info("删除器材请求: equipmentId={}", id);
        equipmentService.deleteEquipment(id);
        return Result.success();
    }

    /**
     * 获取所有报修记录
     *
     * @return 报修记录列表
     */
    @GetMapping("/repairs")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<List<RepairVO>> getAllRepairs() {
        log.info("获取所有报修记录请求");
        List<RepairVO> repairs = equipmentService.getAllRepairs();
        return Result.success(repairs);
    }

    /**
     * 处理报修
     *
     * @param repairId 报修ID
     * @param status   状态
     * @return 操作结果
     */
    @PutMapping("/repairs/{repairId}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> handleRepair(@PathVariable Long repairId, @RequestParam Integer status) {
        log.info("处理报修请求: repairId={}, status={}", repairId, status);
        equipmentService.handleRepair(repairId, status);
        return Result.success();
    }
}
