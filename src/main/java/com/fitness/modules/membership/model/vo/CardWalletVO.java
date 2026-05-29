package com.fitness.modules.membership.model.vo;

import lombok.Data;

@Data
public class CardWalletVO {

    /**
     * NONE - 无会员无订单
     * PENDING_ORDER - 有待支付订单
     * PAID_UNACTIVATED - 已支付未激活
     * ACTIVATED - 已激活
     */
    private String walletStatus;
    private UserMembershipVO membership;
    private PendingOrderItem pendingOrder;
    private PaidOrderItem paidOrder;

    @Data
    public static class PendingOrderItem {
        private String orderNo;
        private String cardName;
        private String price;
    }

    @Data
    public static class PaidOrderItem {
        private String orderNo;
        private String cardName;
        private String price;
        private Integer durationDays;
    }
}
