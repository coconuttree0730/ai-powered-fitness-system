package com.fitness.modules.order.model.enums;

import lombok.Getter;

@Getter
public enum PayMethodEnum {

    ALIPAY("ALIPAY", "支付宝"),
    BALANCE("BALANCE", "余额支付");

    private final String code;
    private final String label;

    PayMethodEnum(String code, String label) {
        this.code = code;
        this.label = label;
    }

    public static PayMethodEnum getByCode(String code) {
        if (code == null) {
            return null;
        }
        for (PayMethodEnum method : values()) {
            if (method.getCode().equals(code)) {
                return method;
            }
        }
        return null;
    }

    public static String getLabelByCode(String code) {
        PayMethodEnum method = getByCode(code);
        return method != null ? method.getLabel() : code;
    }
}