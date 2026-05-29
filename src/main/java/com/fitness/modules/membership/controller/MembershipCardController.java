package com.fitness.modules.membership.controller;

import com.fitness.common.result.Result;
import com.fitness.integration.security.SecurityUtils;
import com.fitness.modules.membership.model.dto.ActivateMembershipDTO;
import com.fitness.modules.membership.model.dto.MembershipOrderDTO;
import com.fitness.modules.membership.model.dto.PayOrderDTO;
import com.fitness.modules.membership.model.vo.AlipayPayVO;
import com.fitness.modules.membership.model.vo.CardWalletVO;
import com.fitness.modules.membership.model.vo.MembershipCardVO;
import com.fitness.modules.membership.model.vo.MembershipOrderVO;
import com.fitness.modules.membership.model.vo.PurchaseCheckVO;
import com.fitness.modules.membership.model.vo.UserMembershipVO;
import com.fitness.modules.membership.service.MembershipCardService;
import com.fitness.modules.membership.service.MembershipOrderService;
import com.fitness.modules.membership.service.UserMembershipService;
import com.fitness.modules.order.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/membership")
@RequiredArgsConstructor
@Tag(name = "会员卡", description = "会员卡用户端接口")
public class MembershipCardController {

    private final MembershipCardService cardService;
    private final MembershipOrderService orderService;
    private final UserMembershipService userMembershipService;
    private final OrderService unifiedOrderService;

    @Operation(summary = "获取会员卡列表")
    @GetMapping("/cards")
    public Result<List<MembershipCardVO>> listActiveCards() {
        return Result.success(cardService.listActiveCards());
    }

    @Operation(summary = "获取推荐会员卡")
    @GetMapping("/cards/recommend")
    public Result<List<MembershipCardVO>> listRecommendCards(@RequestParam(defaultValue = "4") Integer limit) {
        return Result.success(cardService.listRecommendCards(limit));
    }

    @Operation(summary = "获取会员卡详情")
    @GetMapping("/cards/{id}")
    public Result<MembershipCardVO> getCardDetail(@PathVariable Long id) {
        return Result.success(cardService.getCardDetail(id));
    }

    @Operation(summary = "创建订单")
    @PostMapping("/orders")
    @PreAuthorize("isAuthenticated()")
    public Result<MembershipOrderVO> createOrder(@Valid @RequestBody MembershipOrderDTO dto) {
        Long userId = SecurityUtils.getCurrentUserId();
        return Result.success(orderService.createOrder(dto, userId));
    }

    @Operation(summary = "支付订单")
    @PostMapping("/orders/pay")
    @PreAuthorize("isAuthenticated()")
    public Result<AlipayPayVO> payOrder(@Valid @RequestBody PayOrderDTO dto) {
        Long userId = SecurityUtils.getCurrentUserId();
        return Result.success(unifiedOrderService.payOrder(dto.getOrderNo(), dto.getPayMethod(), userId));
    }

    @Operation(summary = "获取订单详情")
    @GetMapping("/orders/{orderNo}")
    @PreAuthorize("isAuthenticated()")
    public Result<MembershipOrderVO> getOrderDetail(@PathVariable String orderNo) {
        Long userId = SecurityUtils.getCurrentUserId();
        return Result.success(orderService.getOrderDetail(orderNo, userId));
    }

    /**
     * 获取我的订单列表
     */
    @GetMapping("/orders/my")
    @PreAuthorize("isAuthenticated()")
    public Result<List<MembershipOrderVO>> getMyOrders() {
        Long userId = SecurityUtils.getCurrentUserId();
        return Result.success(orderService.getUserOrders(userId));
    }

    @Operation(summary = "取消订单")
    @PostMapping("/orders/{orderNo}/cancel")
    @PreAuthorize("isAuthenticated()")
    public Result<Void> cancelOrder(@PathVariable String orderNo) {
        Long userId = SecurityUtils.getCurrentUserId();
        orderService.cancelOrder(orderNo, userId);
        return Result.success();
    }

    /**
     * 获取我的会员信息
     */
    @GetMapping("/my")
    @PreAuthorize("isAuthenticated()")
    public Result<UserMembershipVO> getMyMembership() {
        Long userId = SecurityUtils.getCurrentUserId();
        return Result.success(userMembershipService.getUserMembership(userId));
    }

    @Operation(summary = "检查会员是否有效")
    @GetMapping("/check")
    @PreAuthorize("isAuthenticated()")
    public Result<Boolean> checkMembershipValid() {
        Long userId = SecurityUtils.getCurrentUserId();
        return Result.success(userMembershipService.checkMembershipValid(userId));
    }

    @Operation(summary = "购卡前校验")
    @GetMapping("/cards/{cardId}/purchase-check")
    @PreAuthorize("isAuthenticated()")
    public Result<PurchaseCheckVO> checkPurchase(@PathVariable Long cardId) {
        Long userId = SecurityUtils.getCurrentUserId();
        return Result.success(userMembershipService.checkPurchaseEligibility(userId, cardId));
    }

    @Operation(summary = "激活会员卡")
    @PostMapping("/activate")
    @PreAuthorize("isAuthenticated()")
    public Result<Void> activateMembership(@Valid @RequestBody ActivateMembershipDTO dto) {
        Long userId = SecurityUtils.getCurrentUserId();
        orderService.activateOrder(dto.getOrderNo(), userId);
        return Result.success();
    }

    @Operation(summary = "获取会员卡钱包信息")
    @GetMapping("/card-wallet")
    @PreAuthorize("isAuthenticated()")
    public Result<CardWalletVO> getCardWallet() {
        Long userId = SecurityUtils.getCurrentUserId();
        return Result.success(orderService.getCardWallet(userId));
    }
}
