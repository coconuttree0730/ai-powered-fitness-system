package com.fitness.modules.equipment.controller;

import com.fitness.common.result.Result;
//import com.fitness.integration.security.SecurityUtils;
import com.fitness.integration.security.UserDetailsImpl;
import com.fitness.modules.equipment.model.dto.RepairDTO;
import com.fitness.modules.equipment.model.vo.MyRepairVO;
import com.fitness.modules.equipment.model.vo.RepairVO;
import com.fitness.modules.equipment.service.EquipmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/repairs")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
@Tag(name = "器材维修", description = "器材报修用户端接口")
public class RepairController {

    private final EquipmentService equipmentService;

    /**
     * 提交报修申请
     *
     * @param userDetails 当前登录用户
     * @param dto         报修信息
     * @return 报修记录ID
     */
    @PostMapping
    public Result<Long> submitRepair(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                     @Valid @RequestBody RepairDTO dto) {
        Long userId = userDetails.getId();
        log.info("提交报修申请: userId={}", userId);
        Long repairId = equipmentService.submitRepair(userId, dto);
        return Result.success(repairId);
    }

    @Operation(summary = "获取我的报修记录")
    @GetMapping("/my")
    public Result<List<MyRepairVO>> getMyRepairs(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        Long userId = userDetails.getId();
        log.info("获取我的报修记录: userId={}", userId);
        List<MyRepairVO> repairs = equipmentService.getMyRepairs(userId);
        return Result.success(repairs);
    }

    @Operation(summary = "获取报修详情")
    @GetMapping("/{repairId}")
    public Result<RepairVO> getRepairDetail(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                            @PathVariable Long repairId) {
        Long userId = userDetails.getId();
        log.info("获取报修详情: userId={}, repairId={}", userId, repairId);
        RepairVO repair = equipmentService.getRepairDetail(userId, repairId);
        return Result.success(repair);
    }

    @Operation(summary = "取消报修")
    @PutMapping("/{repairId}/cancel")
    public Result<Void> cancelRepair(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                     @PathVariable Long repairId) {
        Long userId = userDetails.getId();
        log.info("取消报修: userId={}, repairId={}", userId, repairId);
        equipmentService.cancelRepair(userId, repairId);
        return Result.success();
    }
}
