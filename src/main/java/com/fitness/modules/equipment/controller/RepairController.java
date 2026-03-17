package com.fitness.modules.equipment.controller;

import com.fitness.common.result.Result;
import com.fitness.modules.equipment.model.dto.RepairDTO;
import com.fitness.modules.equipment.model.vo.MyRepairVO;
import com.fitness.modules.equipment.service.EquipmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 器材报修控制器（学员端）
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/repairs")
@RequiredArgsConstructor
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
    public Result<Long> submitRepair(@AuthenticationPrincipal UserDetails userDetails,
                                     @Valid @RequestBody RepairDTO dto) {
        Long userId = Long.parseLong(userDetails.getUsername());
        log.info("提交报修申请: userId={}, equipmentId={}", userId, dto.getEquipmentId());
        Long repairId = equipmentService.submitRepair(userId, dto);
        return Result.success(repairId);
    }

    /**
     * 获取我的报修记录
     *
     * @param userDetails 当前登录用户
     * @return 报修记录列表
     */
    @GetMapping("/my")
    public Result<List<MyRepairVO>> getMyRepairs(@AuthenticationPrincipal UserDetails userDetails) {
        Long userId = Long.parseLong(userDetails.getUsername());
        log.info("获取我的报修记录: userId={}", userId);
        List<MyRepairVO> repairs = equipmentService.getMyRepairs(userId);
        return Result.success(repairs);
    }

    /**
     * 取消报修
     *
     * @param userDetails 当前登录用户
     * @param repairId    报修ID
     * @return 成功响应
     */
    @PutMapping("/{repairId}/cancel")
    public Result<Void> cancelRepair(@AuthenticationPrincipal UserDetails userDetails,
                                     @PathVariable Long repairId) {
        Long userId = Long.parseLong(userDetails.getUsername());
        log.info("取消报修: userId={}, repairId={}", userId, repairId);
        equipmentService.cancelRepair(userId, repairId);
        return Result.success();
    }
}
