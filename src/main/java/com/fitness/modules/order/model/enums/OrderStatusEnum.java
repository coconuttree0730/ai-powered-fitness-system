package com.fitness.modules.order.model.enums;

import lombok.Getter;

@Getter
public enum OrderStatusEnum {

    PENDING("PENDING", "待支付"),
    PAID("PAID", "已支付"),
    PROCESSING("PROCESSING", "处理中"),
    NOT_PICKED("NOT_PICKED", "待取货"),
    SHIPPED("SHIPPED", "已发货"),
    COMPLETED("COMPLETED", "已完成"),
    CANCELLED("CANCELLED", "已取消"),
    TIMEOUT("TIMEOUT", "已超时"),
    ACTIVATED("ACTIVATED", "已激活");

    private final String code;
    private final String label;

    OrderStatusEnum(String code, String label) {
        this.code = code;
        this.label = label;
    }

    public static OrderStatusEnum getByCode(String code) {
        if (code == null) {
            return null;
        }
        for (OrderStatusEnum status : values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        return null;
    }

    public static String getLabelByCode(String code) {
        OrderStatusEnum status = getByCode(code);
        return status != null ? status.getLabel() : code;
    }
}