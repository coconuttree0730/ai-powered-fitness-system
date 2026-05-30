package com.fitness.modules.order.model.enums;

import lombok.Getter;

@Getter
public enum OrderTypeEnum {

    PRODUCT("PRODUCT", "商品订单"),
    COACH_PACKAGE("COACH_PACKAGE", "教练套餐订单"),
    MEMBERSHIP("MEMBERSHIP", "会员卡订单");

    private final String code;
    private final String label;

    OrderTypeEnum(String code, String label) {
        this.code = code;
        this.label = label;
    }

    public static OrderTypeEnum getByCode(String code) {
        if (code == null) {
            return null;
        }
        for (OrderTypeEnum type : values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        return null;
    }

    public static String getLabelByCode(String code) {
        OrderTypeEnum type = getByCode(code);
        return type != null ? type.getLabel() : code;
    }
}