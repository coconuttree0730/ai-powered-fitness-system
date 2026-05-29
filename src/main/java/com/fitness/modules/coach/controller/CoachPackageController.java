package com.fitness.modules.coach.controller;

import com.fitness.common.result.Result;
import com.fitness.integration.security.SecurityUtils;
import com.fitness.modules.coach.model.dto.CoachPackageDTO;
import com.fitness.modules.coach.model.vo.CoachPackageVO;
import com.fitness.modules.coach.service.CoachPackageService;
import com.fitness.modules.order.model.dto.OrderDTO;
import com.fitness.modules.order.model.vo.OrderVO;
import com.fitness.modules.order.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 教练套餐控制器
 */
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class CoachPackageController {

    private final CoachPackageService coachPackageService;
    private final OrderService orderService;

    /**
     * 教练查看自己的套餐列表
     */
    @GetMapping("/coach/coach-packages")
    @PreAuthorize("hasRole('COACH')")
    public Result<List<CoachPackageVO>> getMyCoachPackages() {
        Long coachId = SecurityUtils.getCurrentUserId();
        List<CoachPackageVO> packages = coachPackageService.getCoachPackages(coachId);
        return Result.success(packages);
    }

    /**
     * 教练创建套餐
     */
    @PostMapping("/coach/coach-packages")
    @PreAuthorize("hasRole('COACH')")
    public Result<CoachPackageVO> createPackage(@Valid @RequestBody CoachPackageDTO dto) {
        Long coachId = SecurityUtils.getCurrentUserId();
        CoachPackageVO vo = coachPackageService.createPackage(dto, coachId);
        return Result.success(vo);
    }

    /**
     * 教练编辑套餐
     */
    @PutMapping("/coach/coach-packages/{id}")
    @PreAuthorize("hasRole('COACH')")
    public Result<CoachPackageVO> updatePackage(@PathVariable Long id, @Valid @RequestBody CoachPackageDTO dto) {
        Long coachId = SecurityUtils.getCurrentUserId();
        CoachPackageVO vo = coachPackageService.updatePackage(id, dto, coachId);
        return Result.success(vo);
    }

    /**
     * 教练修改套餐状态
     */
    @PutMapping("/coach/coach-packages/{id}/status")
    @PreAuthorize("hasRole('COACH')")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestBody Map<String, String> body) {
        Long coachId = SecurityUtils.getCurrentUserId();
        String status = body.get("status");
        coachPackageService.updateStatus(id, status, coachId);
        return Result.success();
    }

    /**
     * 教练删除（下架）套餐
     */
    @DeleteMapping("/coach/coach-packages/{id}")
    @PreAuthorize("hasRole('COACH')")
    public Result<Void> deletePackage(@PathVariable Long id) {
        Long coachId = SecurityUtils.getCurrentUserId();
        coachPackageService.deletePackage(id, coachId);
        return Result.success();
    }

    /**
     * 所有上架套餐（会员端选购）
     */
    @GetMapping("/coach-packages")
    @PreAuthorize("isAuthenticated()")
    public Result<List<CoachPackageVO>> getAllActivePackages() {
        List<CoachPackageVO> packages = coachPackageService.getAllActivePackages();
        return Result.success(packages);
    }

    /**
     * 某教练的套餐列表
     */
    @GetMapping("/coach-packages/coach/{coachId}")
    public Result<List<CoachPackageVO>> getCoachPackagesByCoachId(@PathVariable Long coachId) {
        List<CoachPackageVO> packages = coachPackageService.getCoachPackages(coachId);
        return Result.success(packages);
    }

    /**
     * 套餐详情
     */
    @GetMapping("/coach-packages/{id}")
    @PreAuthorize("isAuthenticated()")
    public Result<CoachPackageVO> getPackageDetail(@PathVariable Long id) {
        CoachPackageVO vo = coachPackageService.getPackageDetail(id);
        return Result.success(vo);
    }

    /**
     * 会员购买教练套餐（创建订单）
     */
    @PostMapping("/coach-packages/{id}/order")
    @PreAuthorize("isAuthenticated()")
    public Result<OrderVO> purchasePackage(@PathVariable Long id) {
        Long userId = SecurityUtils.getCurrentUserId();
        OrderDTO dto = new OrderDTO();
        dto.setOrderType("COACH_PACKAGE");
        dto.setCoachPackageId(id);
        OrderVO vo = orderService.createOrder(dto, userId);
        return Result.success(vo);
    }
}