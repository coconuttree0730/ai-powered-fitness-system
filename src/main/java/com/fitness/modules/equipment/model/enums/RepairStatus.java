package com.fitness.modules.equipment.model.enums;

import lombok.Getter;

/**
 * 报修状态枚举
 */
@Getter
public enum RepairStatus {

    PENDING(0, "待处理"),
    PROCESSING(1, "处理中"),
    COMPLETED(2, "已完成"),
    CLOSED(3, "已关闭");

    private final Integer code;
    private final String name;

    RepairStatus(Integer code, String name) {
        this.code = code;
        this.name = name;
    }

    /**
     * 根据code获取枚举
     *
     * @param code 状态码
     * @return 枚举值
     */
    public static RepairStatus getByCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (RepairStatus status : values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        return null;
    }

    /**
     * 根据code获取名称
     *
     * @param code 状态码
     * @return 状态名称
     */
    public static String getNameByCode(Integer code) {
        RepairStatus status = getByCode(code);
        return status != null ? status.getName() : "";
    }
}
