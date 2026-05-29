package com.fitness.modules.membership.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.fitness.common.result.PageResult;
import com.fitness.common.result.Result;
import com.fitness.modules.membership.model.dto.MembershipCardDTO;
import com.fitness.modules.membership.model.dto.MembershipCardQueryDTO;
import com.fitness.modules.membership.model.dto.MembershipCardTypeDTO;
import com.fitness.modules.membership.model.entity.UserMembership;
import com.fitness.modules.membership.model.vo.MembershipCardTypeVO;
import com.fitness.modules.membership.model.vo.MembershipCardVO;
import com.fitness.modules.membership.model.vo.MembershipStatsVO;
import com.fitness.modules.membership.service.MembershipCardService;
import com.fitness.modules.membership.service.MembershipCardTypeService;
import com.fitness.modules.membership.service.UserMembershipService;
import com.fitness.modules.order.mapper.OrderMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/membership")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "会员卡管理", description = "会员卡管理后台接口")
public class MembershipCardAdminController {

    private final MembershipCardTypeService cardTypeService;
    private final MembershipCardService cardService;
    private final UserMembershipService userMembershipService;
    private final OrderMapper orderMapper;

    // ==================== 会员卡类型管理 ====================

    @Operation(summary = "创建会员卡类型")
    @PostMapping("/types")
    public Result<MembershipCardTypeVO> createType(@Valid @RequestBody MembershipCardTypeDTO dto) {
        return Result.success(cardTypeService.createType(dto));
    }

    @Operation(summary = "更新会员卡类型")
    @PutMapping("/types/{id}")
    public Result<MembershipCardTypeVO> updateType(@PathVariable Long id, @Valid @RequestBody MembershipCardTypeDTO dto) {
        return Result.success(cardTypeService.updateType(id, dto));
    }

    @Operation(summary = "删除会员卡类型")
    @DeleteMapping("/types/{id}")
    public Result<Void> deleteType(@PathVariable Long id) {
        cardTypeService.deleteType(id);
        return Result.success();
    }

    @Operation(summary = "根据ID获取会员卡类型")
    @GetMapping("/types/{id}")
    public Result<MembershipCardTypeVO> getTypeById(@PathVariable Long id) {
        return Result.success(cardTypeService.getTypeById(id));
    }

    @Operation(summary = "获取所有会员卡类型")
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

    @Operation(summary = "获取所有会员卡")
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

    @Operation(summary = "获取会员统计信息")
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
        BigDecimal monthlyRevenue = orderMapper.sumMembershipRevenueSince(monthStart);
        stats.setMonthlyRevenue(monthlyRevenue);

        long totalPaidOrders = orderMapper.countPaidMembershipOrders();
        long totalOrders = orderMapper.countMembershipOrders();
        BigDecimal renewalRate = totalOrders > 0
                ? BigDecimal.valueOf(totalPaidOrders).multiply(BigDecimal.valueOf(100)).divide(BigDecimal.valueOf(totalOrders), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;
        stats.setRenewalRate(renewalRate);

        return Result.success(stats);
    }
}
