package com.fitness.modules.membership.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.fitness.common.result.PageResult;
import com.fitness.common.result.Result;
import com.fitness.modules.membership.model.dto.MembershipCardDTO;
import com.fitness.modules.membership.model.dto.MembershipCardQueryDTO;
import com.fitness.modules.membership.model.dto.MembershipCardTypeDTO;
import com.fitness.modules.membership.model.entity.MembershipOrder;
import com.fitness.modules.membership.model.entity.UserMembership;
import com.fitness.modules.membership.model.vo.MembershipCardTypeVO;
import com.fitness.modules.membership.model.vo.MembershipCardVO;
import com.fitness.modules.membership.model.vo.MembershipStatsVO;
import com.fitness.modules.membership.service.MembershipCardService;
import com.fitness.modules.membership.service.MembershipCardTypeService;
import com.fitness.modules.membership.service.MembershipOrderService;
import com.fitness.modules.membership.service.UserMembershipService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/api/v1/admin/membership")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class MembershipCardAdminController {

    private final MembershipCardTypeService cardTypeService;
    private final MembershipCardService cardService;
    private final UserMembershipService userMembershipService;
    private final MembershipOrderService orderService;

    // ==================== 会员卡类型管理 ====================

    @PostMapping("/types")
    public Result<MembershipCardTypeVO> createType(@Valid @RequestBody MembershipCardTypeDTO dto) {
        return Result.success(cardTypeService.createType(dto));
    }

    @PutMapping("/types/{id}")
    public Result<MembershipCardTypeVO> updateType(@PathVariable Long id, @Valid @RequestBody MembershipCardTypeDTO dto) {
        return Result.success(cardTypeService.updateType(id, dto));
    }

    @DeleteMapping("/types/{id}")
    public Result<Void> deleteType(@PathVariable Long id) {
        cardTypeService.deleteType(id);
        return Result.success();
    }

    @GetMapping("/types/{id}")
    public Result<MembershipCardTypeVO> getTypeById(@PathVariable Long id) {
        return Result.success(cardTypeService.getTypeById(id));
    }

    @GetMapping("/types")
    public Result<List<MembershipCardTypeVO>> listAllTypes() {
        return Result.success(cardTypeService.listAllTypes());
    }

    // ==================== 会员卡管理 ====================

    @PostMapping("/cards")
    public Result<MembershipCardVO> createCard(@Valid @RequestBody MembershipCardDTO dto) {
        return Result.success(cardService.createCard(dto));
    }

    @PutMapping("/cards/{id}")
    public Result<MembershipCardVO> updateCard(@PathVariable Long id, @Valid @RequestBody MembershipCardDTO dto) {
        return Result.success(cardService.updateCard(id, dto));
    }

    @DeleteMapping("/cards/{id}")
    public Result<Void> deleteCard(@PathVariable Long id) {
        cardService.deleteCard(id);
        return Result.success();
    }

    @GetMapping("/cards/{id}")
    public Result<MembershipCardVO> getCardById(@PathVariable Long id) {
        return Result.success(cardService.getCardDetail(id));
    }

    @GetMapping("/cards")
    public Result<List<MembershipCardVO>> listAllCards() {
        return Result.success(cardService.listAllCards());
    }

    @GetMapping("/cards/active")
    public Result<List<MembershipCardVO>> listActiveCards() {
        return Result.success(cardService.listActiveCards());
    }

    @GetMapping("/cards/page")
    public Result<PageResult<MembershipCardVO>> getCardPage(MembershipCardQueryDTO queryDTO) {
        IPage<MembershipCardVO> page = cardService.getCardPage(queryDTO);
        return Result.success(PageResult.of(page));
    }

    @GetMapping("/stats")
    public Result<MembershipStatsVO> getStats() {
        MembershipStatsVO stats = new MembershipStatsVO();

        long cardTypeCount = cardTypeService.count();
        stats.setCardTypeCount(cardTypeCount);

        long activeMemberCount = userMembershipService.count(
                new LambdaQueryWrapper<UserMembership>()
                        .eq(UserMembership::getIsActive, true)
        );
        stats.setActiveMemberCount(activeMemberCount);

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime monthStart = now.withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
        BigDecimal monthlyRevenue = orderService.getBaseMapper().selectList(
                new LambdaQueryWrapper<MembershipOrder>()
                        .eq(MembershipOrder::getStatus, "PAID")
                        .ge(MembershipOrder::getPayTime, monthStart)
        ).stream()
                .map(MembershipOrder::getPayAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        stats.setMonthlyRevenue(monthlyRevenue);

        long totalPaidOrders = orderService.count(
                new LambdaQueryWrapper<MembershipOrder>()
                        .eq(MembershipOrder::getStatus, "PAID")
        );
        long totalOrders = orderService.count();
        BigDecimal renewalRate = totalOrders > 0
                ? BigDecimal.valueOf(totalPaidOrders).multiply(BigDecimal.valueOf(100)).divide(BigDecimal.valueOf(totalOrders), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;
        stats.setRenewalRate(renewalRate);

        return Result.success(stats);
    }
}
