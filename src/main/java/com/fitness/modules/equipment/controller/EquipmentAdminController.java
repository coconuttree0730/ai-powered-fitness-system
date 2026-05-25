package com.fitness.modules.equipment.controller;

import com.fitness.common.result.Result;
import com.fitness.integration.security.UserDetailsImpl;
import com.fitness.modules.equipment.model.dto.EquipmentDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.fitness.modules.equipment.model.dto.RepairHandleDTO;
import com.fitness.modules.equipment.model.entity.EquipmentType;
import com.fitness.modules.equipment.model.vo.RepairRecordVO;
import com.fitness.modules.equipment.model.vo.RepairVO;
import com.fitness.modules.equipment.service.EquipmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/admin/equipment")
@RequiredArgsConstructor
@Tag(name = "器材管理", description = "器材管理后台接口")
public class EquipmentAdminController {

    private final EquipmentService equipmentService;

    @Operation(summary = "创建器材")
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Long> createEquipment(@Valid @RequestBody EquipmentDTO dto) {
        log.info("创建器材请求: equipmentName={}", dto.getEquipmentName());
        Long equipmentId = equipmentService.createEquipment(dto);
        return Result.success(equipmentId);
    }

    @Operation(summary = "更新器材")
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> updateEquipment(@PathVariable Long id, @Valid @RequestBody EquipmentDTO dto) {
        log.info("更新器材请求: equipmentId={}", id);
        equipmentService.updateEquipment(id, dto);
        return Result.success();
    }

    @Operation(summary = "删除器材")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> deleteEquipment(@PathVariable Long id) {
        log.info("删除器材请求: equipmentId={}", id);
        equipmentService.deleteEquipment(id);
        return Result.success();
    }

    @Operation(summary = "获取所有报修记录")
    @GetMapping("/repairs")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<List<RepairVO>> getAllRepairs() {
        log.info("获取所有报修记录请求");
        List<RepairVO> repairs = equipmentService.getAllRepairs();
        return Result.success(repairs);
    }

    @Operation(summary = "获取报修详情")
    @GetMapping("/repairs/{repairId}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<RepairVO> getRepairDetail(@PathVariable Long repairId) {
        log.info("获取报修详情请求: repairId={}", repairId);
        RepairVO repair = equipmentService.getRepairDetailAdmin(repairId);
        return Result.success(repair);
    }

    @Operation(summary = "处理报修")
    @PutMapping("/repairs/{repairId}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> handleRepair(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                     @PathVariable Long repairId,
                                     @Valid @RequestBody RepairHandleDTO dto) {
        Long handlerId = userDetails.getId();
        log.info("处理报修请求: repairId={}, status={}, handlerId={}", repairId, dto.getStatus(), handlerId);
        equipmentService.handleRepair(repairId, dto, handlerId);
        return Result.success();
    }

    @Operation(summary = "添加处理记录")
    @PostMapping("/repairs/{repairId}/records")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> addRepairRecord(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                        @PathVariable Long repairId,
                                        @RequestParam String content) {
        Long handlerId = userDetails.getId();
        log.info("添加处理记录: repairId={}, handlerId={}", repairId, handlerId);
        equipmentService.addRepairRecord(repairId, content, handlerId);
        return Result.success();
    }

    @Operation(summary = "获取报修处理记录")
    @GetMapping("/repairs/{repairId}/records")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<List<RepairRecordVO>> getRepairRecords(@PathVariable Long repairId) {
        log.info("获取报修处理记录: repairId={}", repairId);
        List<RepairRecordVO> records = equipmentService.getRepairRecords(repairId);
        return Result.success(records);
    }

    /**
     * 删除报修记录
     *
     * @param repairId 报修ID
     * @return 操作结果
     */
    @DeleteMapping("/repairs/{repairId}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> deleteRepair(@PathVariable Long repairId) {
        log.info("删除报修记录请求: repairId={}", repairId);
        equipmentService.deleteRepair(repairId);
        return Result.success();
    }

    @Operation(summary = "获取所有器材类型")
    @GetMapping("/types")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<List<EquipmentType>> getAllEquipmentTypes() {
        log.info("获取所有器材类型请求");
        List<EquipmentType> types = equipmentService.getAllEquipmentTypes();
        return Result.success(types);
    }
}
